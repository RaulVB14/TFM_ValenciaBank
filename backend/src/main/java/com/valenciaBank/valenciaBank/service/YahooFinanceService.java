package com.valenciaBank.valenciaBank.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class YahooFinanceService {

    private static final Logger log = LoggerFactory.getLogger(YahooFinanceService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Yahoo Finance API base URL (pública, sin API key)
    private static final String YAHOO_CHART_URL = "https://query1.finance.yahoo.com/v8/finance/chart/";

    // Mapeo de símbolos amigables a símbolos de Yahoo Finance
    private static final Map<String, String> SYMBOL_MAP = new LinkedHashMap<>();
    static {
        // ETFs Globales
        SYMBOL_MAP.put("VWRL", "VWRL.L");      // London Stock Exchange
        SYMBOL_MAP.put("EUNL", "EUNL.DE");     // Deutsche Börse
        SYMBOL_MAP.put("SWRD", "SWRD.L");      // London Stock Exchange

        // USA Índices ETFs
        SYMBOL_MAP.put("SPY", "SPY");
        SYMBOL_MAP.put("VOO", "VOO");
        SYMBOL_MAP.put("IVV", "IVV");
        SYMBOL_MAP.put("QQQ", "QQQ");
        SYMBOL_MAP.put("VTI", "VTI");
        SYMBOL_MAP.put("IWM", "IWM");

        // Europa
        SYMBOL_MAP.put("VEUR", "VEUR.L");

        // Índices
        SYMBOL_MAP.put("^IBEX", "^IBEX");       // IBEX 35
        SYMBOL_MAP.put("^GSPC", "^GSPC");       // S&P 500
        SYMBOL_MAP.put("^IXIC", "^IXIC");       // NASDAQ
        SYMBOL_MAP.put("^DJI", "^DJI");         // Dow Jones

        // Sectores
        SYMBOL_MAP.put("XLK", "XLK");
        SYMBOL_MAP.put("XLF", "XLF");
        SYMBOL_MAP.put("XLV", "XLV");
        SYMBOL_MAP.put("XLE", "XLE");

        // Dividendos
        SYMBOL_MAP.put("IUSA", "IUSA.L");

        // Acciones populares (de la lista original de Finnhub)
        SYMBOL_MAP.put("AAPL", "AAPL");
        SYMBOL_MAP.put("MSFT", "MSFT");
        SYMBOL_MAP.put("GOOGL", "GOOGL");
        SYMBOL_MAP.put("AMZN", "AMZN");
        SYMBOL_MAP.put("TSLA", "TSLA");
        SYMBOL_MAP.put("META", "META");
        SYMBOL_MAP.put("NVDA", "NVDA");
    }

    @Autowired
    public YahooFinanceService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Obtener cotización actual de un ETF/acción
     * @param symbol Símbolo del activo
     * @return JSON con precio actual y datos básicos
     */
    public String getQuote(String symbol) {
        try {
            String yahooSymbol = resolveSymbol(symbol);
            String url = YAHOO_CHART_URL + yahooSymbol + "?interval=1d&range=1d";

            String response = callYahoo(url);
            JsonNode root = objectMapper.readTree(response);
            JsonNode result = root.path("chart").path("result").get(0);

            if (result == null) {
                return buildError("No se encontraron datos para " + symbol);
            }

            JsonNode meta = result.path("meta");
            double currentPrice = meta.path("regularMarketPrice").asDouble();
            double previousClose = meta.path("chartPreviousClose").asDouble();
            String currency = meta.path("currency").asText("USD");
            String exchangeName = meta.path("exchangeName").asText("");

            double change = currentPrice - previousClose;
            double changePercent = previousClose > 0 ? (change / previousClose) * 100 : 0;

            Map<String, Object> quote = new LinkedHashMap<>();
            quote.put("symbol", symbol);
            quote.put("yahooSymbol", yahooSymbol);
            quote.put("c", currentPrice);            // current price
            quote.put("pc", previousClose);           // previous close
            quote.put("d", Math.round(change * 100.0) / 100.0);  // change
            quote.put("dp", Math.round(changePercent * 100.0) / 100.0); // change percent
            quote.put("currency", currency);
            quote.put("exchange", exchangeName);

            return objectMapper.writeValueAsString(quote);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Yahoo Finance: símbolo no encontrado: {}", symbol);
            return buildNotFound(symbol);
        } catch (HttpClientErrorException e) {
            log.error("Error Yahoo Finance quote para {} ({})", symbol, e.getStatusCode());
            return buildError("Datos no disponibles para " + symbol);
        } catch (Exception e) {
            log.error("Error Yahoo Finance quote para {}: {}", symbol, e.getMessage());
            return buildError("Datos no disponibles para " + symbol);
        }
    }

    /**
     * Obtener datos históricos (velas) de un ETF/acción
     * @param symbol Símbolo del activo
     * @param days Número de días (1, 7, 30, 90, 365)
     * @return JSON con datos históricos en formato compatible con el frontend
     */
    public String getCandles(String symbol, int days) {
        try {
            String yahooSymbol = resolveSymbol(symbol);

            // Determinar range e interval según los días solicitados
            String range;
            String interval;
            if (days <= 1) {
                range = "1d";
                interval = "5m";      // cada 5 minutos para intradía
            } else if (days <= 7) {
                range = "5d";
                interval = "30m";     // cada 30 min para 5 días (Yahoo no soporta 7d)
            } else if (days <= 30) {
                range = "1mo";
                interval = "1d";      // diario para 1 mes
            } else if (days <= 90) {
                range = "3mo";
                interval = "1d";      // diario para 3 meses
            } else {
                range = "1y";
                interval = "1d";      // diario para 1 año
            }

            String url = YAHOO_CHART_URL + yahooSymbol
                    + "?interval=" + interval
                    + "&range=" + range;

            String response = callYahoo(url);
            JsonNode root = objectMapper.readTree(response);
            JsonNode chart = root.path("chart");

            // Verificar error de Yahoo
            if (chart.has("error") && !chart.path("error").isNull()) {
                String errorDesc = chart.path("error").path("description").asText("Error desconocido");
                log.error("Yahoo Finance error: {}", errorDesc);
                return buildError("Yahoo Finance: " + errorDesc);
            }

            JsonNode result = chart.path("result").get(0);
            if (result == null) {
                return buildError("No se encontraron datos históricos para " + symbol);
            }

            // Extraer timestamps
            JsonNode timestamps = result.path("timestamp");
            if (timestamps.isMissingNode() || !timestamps.isArray() || timestamps.size() == 0) {
                return buildError("Sin datos de timestamps para " + symbol);
            }

            // Extraer precios de cierre
            JsonNode closePrices = result.path("indicators").path("quote").get(0).path("close");
            JsonNode openPrices = result.path("indicators").path("quote").get(0).path("open");
            JsonNode highPrices = result.path("indicators").path("quote").get(0).path("high");
            JsonNode lowPrices = result.path("indicators").path("quote").get(0).path("low");
            JsonNode volumes = result.path("indicators").path("quote").get(0).path("volume");

            // Construir respuesta en formato compatible con el frontend (formato Finnhub)
            List<Long> t = new ArrayList<>();
            List<Double> c = new ArrayList<>();
            List<Double> o = new ArrayList<>();
            List<Double> h = new ArrayList<>();
            List<Double> l = new ArrayList<>();
            List<Long> v = new ArrayList<>();

            long nowEpoch = System.currentTimeMillis() / 1000;

            for (int i = 0; i < timestamps.size(); i++) {
                // Filtrar nulls (mercado cerrado, etc.)
                if (closePrices.get(i) == null || closePrices.get(i).isNull()) {
                    continue;
                }

                // Filtrar timestamps futuros (Yahoo pre-genera velas para toda la sesión)
                long ts = timestamps.get(i).asLong();
                if (ts > nowEpoch) {
                    continue;
                }

                t.add(ts);
                c.add(closePrices.get(i).asDouble());
                o.add(openPrices.get(i) != null && !openPrices.get(i).isNull()
                        ? openPrices.get(i).asDouble() : closePrices.get(i).asDouble());
                h.add(highPrices.get(i) != null && !highPrices.get(i).isNull()
                        ? highPrices.get(i).asDouble() : closePrices.get(i).asDouble());
                l.add(lowPrices.get(i) != null && !lowPrices.get(i).isNull()
                        ? lowPrices.get(i).asDouble() : closePrices.get(i).asDouble());
                v.add(volumes.get(i) != null && !volumes.get(i).isNull()
                        ? volumes.get(i).asLong() : 0L);
            }

            if (t.isEmpty()) {
                return buildError("No hay datos válidos para " + symbol + " en el rango solicitado");
            }

            // Formato compatible con el frontend existente (mismo formato que Finnhub)
            Map<String, Object> candles = new LinkedHashMap<>();
            candles.put("s", "ok");  // status
            candles.put("t", t);     // timestamps
            candles.put("c", c);     // close prices
            candles.put("o", o);     // open prices
            candles.put("h", h);     // high prices
            candles.put("l", l);     // low prices
            candles.put("v", v);     // volumes

            // Añadir metadata extra
            JsonNode meta = result.path("meta");
            candles.put("currency", meta.path("currency").asText("USD"));
            candles.put("exchange", meta.path("exchangeName").asText(""));
            candles.put("symbol", symbol);

            log.info("Yahoo Finance candles para {} ({}): {} puntos", symbol, yahooSymbol, t.size());
            return objectMapper.writeValueAsString(candles);

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Yahoo Finance: símbolo no encontrado para históricos: {}", symbol);
            return buildNotFound(symbol);
        } catch (HttpClientErrorException e) {
            log.error("Error Yahoo Finance candles para {} ({})", symbol, e.getStatusCode());
            return buildError("Datos no disponibles para " + symbol);
        } catch (Exception e) {
            log.error("Error Yahoo Finance candles para {}: {}", symbol, e.getMessage());
            return buildError("Datos no disponibles para " + symbol);
        }
    }

    /**
     * Buscar activos por nombre/símbolo
     */
    public String search(String query) {
        try {
            String url = "https://query1.finance.yahoo.com/v1/finance/search?q="
                    + query + "&quotesCount=10&newsCount=0";

            String response = callYahoo(url);
            return response;
        } catch (Exception e) {
            log.error("Error al buscar en Yahoo Finance: {}", e.getMessage());
            return buildError("Error al buscar: " + e.getMessage());
        }
    }

    /**
     * Resolver símbolo del usuario al símbolo de Yahoo Finance
     */
    private String resolveSymbol(String symbol) {
        String upper = symbol.toUpperCase();
        return SYMBOL_MAP.getOrDefault(upper, upper);
    }

    /**
     * Llamar a Yahoo Finance con User-Agent adecuado
     */
    private String callYahoo(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    /**
     * Construir respuesta de error
     */
    private String buildError(String message) {
        return "{\"error\": \"" + message.replace("\"", "'") + "\"}";
    }

    /**
     * Construir respuesta para símbolo no encontrado / delisted
     */
    private String buildNotFound(String symbol) {
        return "{\"error\": \"El fondo '" + symbol + "' no está disponible. Puede haber sido retirado del mercado.\", \"notFound\": true}";
    }
}
