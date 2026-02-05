package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.CryptoHistoryCache;
import com.valenciaBank.valenciaBank.repository.CryptoHistoryCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class CoinGeckoService {

    private final RestTemplate restTemplate;
    private final CryptoHistoryCacheRepository cacheRepository;
    private static final String COINGECKO_API_URL = "https://api.coingecko.com/api/v3";

    @Autowired
    public CoinGeckoService(RestTemplate restTemplate, CryptoHistoryCacheRepository cacheRepository) {
        this.restTemplate = restTemplate;
        this.cacheRepository = cacheRepository;
    }

    /**
     * Obtener datos de una criptomoneda desde CoinGecko
     * @param symbol Símbolo de la cripto (BTC, ETH, etc)
     * @param vsCurrency Moneda de referencia (usd, eur, etc)
     * @return JSON con datos de la criptomoneda
     */
    public String getCryptoData(String symbol, String vsCurrency) {
        try {
            String cryptoId = convertSymbolToCoinGeckoId(symbol);
            String url = String.format(
                "%s/simple/price?ids=%s&vs_currencies=%s&include_market_cap=true&include_24hr_vol=true&include_24hr_change=true",
                COINGECKO_API_URL,
                cryptoId,
                vsCurrency.toLowerCase()
            );

            String response = restTemplate.getForObject(url, String.class);
            System.out.println("CoinGecko response para " + symbol + ": " + (response != null ? response.substring(0, Math.min(100, response.length())) : "null"));
            return response;
        } catch (Exception e) {
            System.err.println("Error al obtener datos de CoinGecko para " + symbol + ": " + e.getMessage());
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
    public String getCryptoHistory(String symbol, int days, String vsCurrency) {
        try {
            String symbolUpper = symbol.toUpperCase();
            String currencyUpper = vsCurrency.toUpperCase();
            
            // 1️⃣ Intentar obtener caché exacto
            Optional<CryptoHistoryCache> cachedData = cacheRepository.findBySymbolAndDaysAndCurrency(
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
            // Orden de preferencia: 30D → 7D → 1D
            System.out.println("🔍 Caché no encontrado para " + days + " días, buscando fallback...");
            
            Optional<CryptoHistoryCache> fallbackCache = Optional.empty();
            int fallbackDays = 0;
            
            // Si pide 1D, buscar caché de 7D o 30D
            if (days == 1) {
                fallbackCache = cacheRepository.findBySymbolAndDaysAndCurrency(symbolUpper, 7, currencyUpper);
                fallbackDays = 7;
                if (!fallbackCache.isPresent()) {
                    fallbackCache = cacheRepository.findBySymbolAndDaysAndCurrency(symbolUpper, 30, currencyUpper);
                    fallbackDays = 30;
                }
            }
            // Si pide 7D, buscar caché de 30D
            else if (days == 7) {
                fallbackCache = cacheRepository.findBySymbolAndDaysAndCurrency(symbolUpper, 30, currencyUpper);
                fallbackDays = 30;
            }
            // Si pide 30D, no hay fallback (es el máximo que cachean)
            // Si pide 90D, 365D, etc., también usan 30D como fallback si existe
            else if (days > 30) {
                fallbackCache = cacheRepository.findBySymbolAndDaysAndCurrency(symbolUpper, 30, currencyUpper);
                fallbackDays = 30;
            }

            // Si encontramos caché de fallback válido, usarlo
            if (fallbackCache.isPresent()) {
                CryptoHistoryCache cache = fallbackCache.get();
                if (cache.isValid()) {
                    System.out.println("📦 Usando CACHÉ de fallback: " + fallbackDays + " días para cubrir " + days + " días");
                    return cache.getHistoryData();
                }
            }

            // 3️⃣ Si no hay caché en ningún rango, llamar a CoinGecko
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
            System.out.println("✅ Datos obtenidos de CoinGecko para " + symbol);

            // Guardar en caché
            CryptoHistoryCache cache = new CryptoHistoryCache(
                symbolUpper,
                days,
                currencyUpper,
                response
            );
            cacheRepository.save(cache);
            System.out.println("💾 Datos cacheados para " + symbol + " (expira en 24 horas)");

            return response;
        } catch (Exception e) {
            System.err.println("❌ Error al obtener historial de CoinGecko para " + symbol + ": " + e.getMessage());
            return "{\"error\": \"Error al obtener historial de CoinGecko\"}";
        }
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
