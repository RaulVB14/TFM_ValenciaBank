package com.valenciaBank.valenciaBank.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class FinnhubService {

    private static final Logger log = LoggerFactory.getLogger(FinnhubService.class);

    private final RestTemplate restTemplate;
    private static final String FINNHUB_API_URL = "https://finnhub.io/api/v1";
    
    // Símbolos que funcionan bien en Finnhub (plan free)
    private static final Set<String> SUPPORTED_SYMBOLS = new HashSet<>(Arrays.asList(
        "SPY", "VOO", "IVV", "VTI", "BND", "AGG", "QQQ", "IWM", "EEM", "VEA", "VWO",
        "XLK", "XLV", "XLF", "XLY", "XLP", "XLI", "XLE", "XLRE", "XLU",
        "AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "META", "NVDA", "JPM", "V", "WMT",
        "JNJ", "PG", "MA", "HD", "COST", "KO", "DIS", "PYPL", "NFLX", "ADBE"
    ));

    @Value("${finnhub.api.key:}")
    private String finnhubApiKey;

    @Autowired
    public FinnhubService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Obtener datos de un ETF/Fondo desde Finnhub
     * @param symbol Símbolo del ETF (VWRL, SPY, etc)
     * @return JSON con datos del ETF
     */
    public String getEtfData(String symbol) {
        try {
            // Si no hay API key configurada, retornar error informativo
            if (finnhubApiKey == null || finnhubApiKey.isEmpty()) {
                return "{\"error\": \"API key de Finnhub no configurada. Obtén una en https://finnhub.io/\"}";
            }

            String url = String.format(
                "%s/quote?symbol=%s&token=%s",
                FINNHUB_API_URL,
                symbol.toUpperCase(),
                finnhubApiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            log.info("Finnhub response para {}: OK", symbol);
            return response;
        } catch (Exception e) {
            log.error("Error al obtener datos de Finnhub para {}: {}", symbol, e.getMessage());
            return "{\"error\": \"Error al obtener datos de Finnhub: " + e.getMessage() + "\"}";
        }
    }

    /**
     * Obtener datos históricos de un ETF/Fondo
     * @param symbol Símbolo del ETF
     * @param resolution Resolución (1=1min, 5=5min, 15=15min, 30=30min, 60=1hour, D=daily, W=weekly, M=monthly)
     * @param days Número de días a obtener
     * @return JSON con datos históricos
     */
    public String getEtfCandles(String symbol, String resolution, int days) {
        try {
            if (finnhubApiKey == null || finnhubApiKey.isEmpty()) {
                return "{\"error\": \"API key de Finnhub no configurada\"}";
            }

            // Validar si el símbolo está soportado en el plan free
            String upperSymbol = symbol.toUpperCase();
            if (!SUPPORTED_SYMBOLS.contains(upperSymbol)) {
                log.warn("Símbolo no soportado en Finnhub free tier: {}", upperSymbol);
                return "{\"error\": \"El símbolo '" + upperSymbol + "' no está disponible en el plan free de Finnhub. Símbolos soportados: SPY, VOO, IVV, QQQ, etc.\"}";
            }

            long toTimestamp = System.currentTimeMillis() / 1000;
            long fromTimestamp = toTimestamp - (days * 86400L);

            String url = String.format(
                "%s/stock/candle?symbol=%s&resolution=%s&from=%d&to=%d&token=%s",
                FINNHUB_API_URL,
                upperSymbol,
                resolution,
                fromTimestamp,
                toTimestamp,
                finnhubApiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            log.info("Finnhub candles obtenidos para {}", upperSymbol);
            return response;
        } catch (Exception e) {
            log.error("Error al obtener velas de Finnhub para {}: {}", symbol, e.getMessage());
            return "{\"error\": \"Error al obtener datos históricos de Finnhub\"}";
        }
    }

    /**
     * Buscar símbolo de ETF por nombre
     * @param query Nombre o símbolo parcial del ETF
     * @return JSON con resultados de búsqueda
     */
    public String searchEtf(String query) {
        try {
            if (finnhubApiKey == null || finnhubApiKey.isEmpty()) {
                return "{\"error\": \"API key de Finnhub no configurada\"}";
            }

            String url = String.format(
                "%s/search?q=%s&token=%s",
                FINNHUB_API_URL,
                query,
                finnhubApiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            log.info("Finnhub search response para {}: OK", query);
            return response;
        } catch (Exception e) {
            log.error("Error al buscar en Finnhub: {}", e.getMessage());
            return "{\"error\": \"Error al buscar ETF\"}";
        }
    }
}
