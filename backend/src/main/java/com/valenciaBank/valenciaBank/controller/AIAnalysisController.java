package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.service.AIAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for AI-powered market analysis.
 * Provides trend analysis for cryptocurrencies and ETFs using Google Gemini.
 */
@RestController
@RequestMapping("/api/ai")
public class AIAnalysisController {

    @Autowired
    private AIAnalysisService aiAnalysisService;

    /**
     * POST /api/ai/analyze
     * 
     * Request body:
     * {
     *   "symbol": "BTC",
     *   "type": "crypto" | "etf",
     *   "prices": [95000.5, 95200.3, ...],
     *   "language": "es"
     * }
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeMarketTrend(@RequestBody Map<String, Object> request) {
        try {
            String symbol = (String) request.getOrDefault("symbol", "BTC");
            String type = (String) request.getOrDefault("type", "crypto");
            String language = (String) request.getOrDefault("language", "es");

            @SuppressWarnings("unchecked")
            List<Double> prices = ((List<Number>) request.getOrDefault("prices", List.of()))
                    .stream()
                    .map(Number::doubleValue)
                    .toList();

            if (prices.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Se requieren datos de precios para el análisis"
                ));
            }

            String analysis = aiAnalysisService.generateAnalysis(symbol, type, prices, language);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "symbol", symbol,
                "analysis", analysis
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error al generar análisis: " + e.getMessage()
            ));
        }
    }
}
