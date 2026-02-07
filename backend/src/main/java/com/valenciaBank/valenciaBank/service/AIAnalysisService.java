package com.valenciaBank.valenciaBank.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Service that integrates with Groq API (Llama 3.3 70B) to provide
 * AI-powered market trend analysis for cryptocurrencies and ETFs.
 * Groq free tier: 30 requests/min, 14,400 requests/day.
 */
@Service
public class AIAnalysisService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    @Autowired
    public AIAnalysisService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Generates a market trend analysis for a given asset using Groq AI (Llama 3.3).
     *
     * @param symbol   The asset symbol (e.g., BTC, ETH, SPY)
     * @param type     "crypto" or "etf"
     * @param prices   Recent price data points
     * @param language The language for the response (default: "es")
     * @return AI-generated analysis text
     */
    public String generateAnalysis(String symbol, String type, List<Double> prices, String language) {
        if (groqApiKey == null || groqApiKey.isEmpty()) {
            return "⚠️ API key de Groq no configurada. Consigue una gratis en https://console.groq.com y añade groq.api.key en application.properties";
        }

        try {
            String prompt = buildPrompt(symbol, type, prices, language);

            Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(
                    Map.of("role", "system", "content",
                        "Eres un analista financiero experto. Respondes de forma concisa con emojis. No uses markdown."),
                    Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7,
                "max_tokens", 400
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                GROQ_API_URL, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return extractTextFromResponse(response.getBody());
            } else {
                return "❌ Error al obtener análisis. Código: " + response.getStatusCode();
            }

        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("429")) {
                return "⏳ Límite de peticiones alcanzado. Espera unos segundos e inténtalo de nuevo.";
            }
            if (msg != null && msg.contains("401")) {
                return "⚠️ API key de Groq inválida. Revisa groq.api.key en application.properties";
            }
            return "❌ Error al conectar con Groq AI: " + msg;
        }
    }

    private String buildPrompt(String symbol, String type, List<Double> prices, String language) {
        String assetType = "crypto".equals(type) ? "criptomoneda" : "fondo indexado/ETF";

        double currentPrice = prices.isEmpty() ? 0 : prices.get(prices.size() - 1);
        double minPrice = prices.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maxPrice = prices.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double firstPrice = prices.isEmpty() ? 0 : prices.get(0);
        double changePercent = firstPrice > 0 ? ((currentPrice - firstPrice) / firstPrice) * 100 : 0;

        return String.format(
            "Analiza la tendencia del %s %s. " +
            "Datos: precio actual %.4f, mínimo %.4f, máximo %.4f, variación %.2f%%. " +
            "Incluye: " +
            "1. Tendencia (alcista/bajista/lateral) con emoji " +
            "2. Soporte y resistencia aproximados " +
            "3. Recomendación breve (2 líneas máximo) " +
            "Responde en %s. Máximo 150 palabras. Solo texto plano con emojis.",
            assetType, symbol, currentPrice, minPrice, maxPrice, changePercent,
            "es".equals(language) ? "español" : "English"
        );
    }

    /**
     * Extracts the assistant message text from an OpenAI-compatible response.
     */
    private String extractTextFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText();
            }
            return "No se pudo generar el análisis.";
        } catch (Exception e) {
            return "Error al procesar respuesta de IA: " + e.getMessage();
        }
    }
}
