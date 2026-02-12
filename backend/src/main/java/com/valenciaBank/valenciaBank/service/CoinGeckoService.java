package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.CryptoHistoryCache;
import com.valenciaBank.valenciaBank.repository.CryptoHistoryCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class CoinGeckoService {

    private final RestTemplate restTemplate;
    private final CryptoHistoryCacheRepository cacheRepository;
    private static final String COINGECKO_API_URL = "https://api.coingecko.com/api/v3";

    // Control de rate limit: tiempo mínimo entre llamadas a CoinGecko (ms)
    private static final long MIN_REQUEST_INTERVAL_MS = 1500;
    private static long lastRequestTime = 0;
    private static boolean rateLimitHit = false;

    @Autowired
    public CoinGeckoService(RestTemplate restTemplate, CryptoHistoryCacheRepository cacheRepository) {
        this.restTemplate = restTemplate;
        this.cacheRepository = cacheRepository;
    }

    /**
     * Esperar si es necesario para respetar el rate limit de CoinGecko
     */
    private synchronized void waitForRateLimit() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestTime;
        if (elapsed < MIN_REQUEST_INTERVAL_MS) {
            try {
                long waitTime = MIN_REQUEST_INTERVAL_MS - elapsed;
                System.out.println("⏳ Esperando " + waitTime + "ms para respetar rate limit de CoinGecko...");
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestTime = System.currentTimeMillis();
    }

    /**
     * Obtener datos de una criptomoneda desde CoinGecko
     * @param symbol Símbolo de la cripto (BTC, ETH, etc)
     * @param vsCurrency Moneda de referencia (usd, eur, etc)
     * @return JSON con datos de la criptomoneda
     */
    public String getCryptoData(String symbol, String vsCurrency) {
        // Si ya estamos en rate limit, no intentar
        if (rateLimitHit) {
            System.out.println("⚠️ Rate limit activo, saltando llamada a CoinGecko para " + symbol);
            return "{\"error\": \"Rate limit activo\"}";
        }
        try {
            waitForRateLimit();
            String cryptoId = convertSymbolToCoinGeckoId(symbol);
            String url = String.format(
                "%s/simple/price?ids=%s&vs_currencies=%s&include_market_cap=true&include_24hr_vol=true&include_24hr_change=true",
                COINGECKO_API_URL,
                cryptoId,
                vsCurrency.toLowerCase()
            );

            String response = restTemplate.getForObject(url, String.class);
            rateLimitHit = false; // Reset si la llamada fue exitosa
            System.out.println("CoinGecko response para " + symbol + ": " + (response != null ? response.substring(0, Math.min(100, response.length())) : "null"));
            return response;
        } catch (HttpClientErrorException.TooManyRequests e) {
            rateLimitHit = true;
            System.err.println("⚠️ 429 Too Many Requests para " + symbol + " - usando fallback");
            return "{\"error\": \"Rate limit exceeded\"}";
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("429")) {
                rateLimitHit = true;
                System.err.println("⚠️ 429 Too Many Requests para " + symbol + " - usando fallback");
            } else {
                System.err.println("Error al obtener datos de CoinGecko para " + symbol + ": " + e.getMessage());
            }
            return "{\"error\": \"Error al obtener datos de CoinGecko\"}";
        }
    }

    /**
     * Obtener datos históricos de una criptomoneda
     * Primero verifica el caché, si no existe o está expirado, llama a CoinGecko
     * Estrategia inteligente: si no hay caché exacto, busca caché de otros rangos
     * @param symbol Símbolo de la cripto
     * @param days Número de días (1, 7, 30, 90, 365, etc)
     * @param vsCurrency Moneda de referencia
     * @return JSON con datos históricos
     */
    @Transactional
    public String getCryptoHistory(String symbol, int days, String vsCurrency) {
        try {
            String symbolUpper = symbol.toUpperCase();
            String currencyUpper = vsCurrency.toUpperCase();
            
            // 1️⃣ Intentar obtener caché exacto
            Optional<CryptoHistoryCache> cachedData = cacheRepository.findFirstBySymbolAndDaysAndCurrency(
                symbolUpper,
                days,
                currencyUpper
            );

            if (cachedData.isPresent()) {
                CryptoHistoryCache cache = cachedData.get();
                if (cache.isValid()) {
                    System.out.println("📦 Datos de " + symbol + " obtenidos desde CACHÉ (exacto: " + days + " días)");
                    return cache.getHistoryData();
                }
            }

            // 2️⃣ Si no hay caché exacto, buscar caché más grande (fallback inteligente)
            System.out.println("🔍 Caché no encontrado para " + days + " días, buscando fallback...");
            
            Optional<CryptoHistoryCache> fallbackCache = Optional.empty();
            int fallbackDays = 0;
            
            if (days == 1) {
                fallbackCache = cacheRepository.findFirstBySymbolAndDaysAndCurrency(symbolUpper, 7, currencyUpper);
                fallbackDays = 7;
                if (!fallbackCache.isPresent()) {
                    fallbackCache = cacheRepository.findFirstBySymbolAndDaysAndCurrency(symbolUpper, 30, currencyUpper);
                    fallbackDays = 30;
                }
            } else if (days == 7) {
                fallbackCache = cacheRepository.findFirstBySymbolAndDaysAndCurrency(symbolUpper, 30, currencyUpper);
                fallbackDays = 30;
            } else if (days > 30) {
                fallbackCache = cacheRepository.findFirstBySymbolAndDaysAndCurrency(symbolUpper, 30, currencyUpper);
                fallbackDays = 30;
            }

            if (fallbackCache.isPresent()) {
                CryptoHistoryCache cache = fallbackCache.get();
                if (cache.isValid()) {
                    System.out.println("📦 Usando CACHÉ de fallback: " + fallbackDays + " días para cubrir " + days + " días");
                    return cache.getHistoryData();
                }
            }

            // 3️⃣ Si estamos en rate limit, usar caché expirada antes de intentar la API
            if (rateLimitHit) {
                String expiredData = getExpiredCacheData(symbolUpper, days, currencyUpper);
                if (expiredData != null) {
                    System.out.println("📦 Rate limit activo → usando caché expirada para " + symbol);
                    return expiredData;
                }
            }

            // 4️⃣ Llamar a CoinGecko API
            waitForRateLimit();
            System.out.println("🔄 Obteniendo datos históricos de CoinGecko para " + symbol + "...");
            String cryptoId = convertSymbolToCoinGeckoId(symbol);
            String url = String.format(
                "%s/coins/%s/market_chart?vs_currency=%s&days=%d",
                COINGECKO_API_URL,
                cryptoId,
                vsCurrency.toLowerCase(),
                days
            );

            String response = restTemplate.getForObject(url, String.class);
            rateLimitHit = false;
            System.out.println("✅ Datos obtenidos de CoinGecko para " + symbol);

            // Eliminar caché antiguo y guardar nuevo
            cacheRepository.deleteBySymbolAndDaysAndCurrency(symbolUpper, days, currencyUpper);
            cacheRepository.flush(); // Forzar ejecución del DELETE antes del INSERT
            
            // Verificar si ya existe (por concurrencia) y actualizar en lugar de insertar
            Optional<CryptoHistoryCache> existing = cacheRepository.findFirstBySymbolAndDaysAndCurrency(symbolUpper, days, currencyUpper);
            if (existing.isPresent()) {
                CryptoHistoryCache cache = existing.get();
                cache.setHistoryData(response);
                cache.refreshExpiration();
                cacheRepository.save(cache);
            } else {
                CryptoHistoryCache cache = new CryptoHistoryCache(
                    symbolUpper,
                    days,
                    currencyUpper,
                    response
                );
                cacheRepository.save(cache);
            }
            System.out.println("💾 Datos cacheados para " + symbol + " (expira en 24 horas)");

            return response;

        } catch (HttpClientErrorException.TooManyRequests e) {
            rateLimitHit = true;
            System.err.println("⚠️ 429 Too Many Requests para historial de " + symbol + " - buscando caché expirada...");
            return handleRateLimitFallback(symbol, days, vsCurrency);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("429")) {
                rateLimitHit = true;
                System.err.println("⚠️ 429 Too Many Requests para historial de " + symbol + " - buscando caché expirada...");
                return handleRateLimitFallback(symbol, days, vsCurrency);
            }
            System.err.println("❌ Error al obtener historial de CoinGecko para " + symbol + ": " + e.getMessage());
            // Último intento: usar caché expirada
            String expired = getExpiredCacheData(symbol.toUpperCase(), days, vsCurrency.toUpperCase());
            if (expired != null) return expired;
            return "{\"error\": \"Error al obtener historial de CoinGecko\"}";
        }
    }

    /**
     * Fallback cuando CoinGecko devuelve 429: buscar cualquier caché disponible (incluso expirada)
     */
    private String handleRateLimitFallback(String symbol, int days, String vsCurrency) {
        String symbolUpper = symbol.toUpperCase();
        String currencyUpper = vsCurrency.toUpperCase();

        String data = getExpiredCacheData(symbolUpper, days, currencyUpper);
        if (data != null) return data;

        return "{\"error\": \"Rate limit y sin caché disponible\"}";
    }

    /**
     * Buscar caché aunque esté expirada (para fallback en caso de 429)
     * Busca: exacto → 30D → 7D → 1D → 365D
     */
    private String getExpiredCacheData(String symbolUpper, int days, String currencyUpper) {
        // Buscar exacto (expirado)
        Optional<CryptoHistoryCache> cache = cacheRepository.findFirstBySymbolAndDaysAndCurrency(symbolUpper, days, currencyUpper);
        if (cache.isPresent()) {
            System.out.println("📦 Usando caché EXPIRADA de " + symbolUpper + " (" + days + " días)");
            return cache.get().getHistoryData();
        }
        // Buscar otros rangos expirados
        int[] fallbackOrder = {30, 7, 1, 365};
        for (int fb : fallbackOrder) {
            if (fb == days) continue;
            cache = cacheRepository.findFirstBySymbolAndDaysAndCurrency(symbolUpper, fb, currencyUpper);
            if (cache.isPresent()) {
                System.out.println("📦 Usando caché EXPIRADA de " + symbolUpper + " (fallback: " + fb + " días)");
                return cache.get().getHistoryData();
            }
        }
        return null;
    }

    /**
     * Convertir símbolo a ID de CoinGecko
     */
    public String convertSymbolToCoinGeckoId(String symbol) {
        switch (symbol.toUpperCase()) {
            case "BTC": return "bitcoin";
            case "ETH": return "ethereum";
            case "ADA": return "cardano";
            case "SOL": return "solana";
            case "XRP": return "ripple";
            case "DOT": return "polkadot";
            case "DOGE": return "dogecoin";
            case "LINK": return "chainlink";
            case "MATIC": return "matic-network";
            case "UNI": return "uniswap";
            case "AVAX": return "avalanche-2";
            case "LTC": return "litecoin";
            case "BCH": return "bitcoin-cash";
            case "ETC": return "ethereum-classic";
            case "XLM": return "stellar";
            case "ATOM": return "cosmos";
            case "NEAR": return "near";
            case "FLOW": return "flow";
            case "THETA": return "theta-token";
            case "VET": return "vechain";
            case "USDT": return "tether";
            case "USDC": return "usd-coin";
            case "BNB": return "binancecoin";
            case "SHIB": return "shiba-inu";
            case "PEPE": return "pepe";
            case "FLOKI": return "floki";
            case "XMR": return "monero";
            case "ZEC": return "zcash";
            case "DASH": return "dash";
            case "ARB": return "arbitrum";
            case "OP": return "optimism";
            case "LINEA": return "linea";
            case "AAVE": return "aave";
            case "CRV": return "curve-dao-token";
            case "SUSHI": return "sushi";
            case "FIL": return "filecoin";
            case "ICP": return "internet-computer";
            case "RUNE": return "thorchain";
            case "GRT": return "the-graph";
            case "AXS": return "axie-infinity";
            case "SAND": return "the-sandbox";
            case "MANA": return "decentraland";
            case "ALGO": return "algorand";
            default: return symbol.toLowerCase();
        }
    }
}
