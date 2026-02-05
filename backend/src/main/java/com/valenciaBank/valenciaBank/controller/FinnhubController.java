package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.service.FinnhubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finnhub")
public class FinnhubController {

    @Autowired
    private FinnhubService finnhubService;

    /**
     * Obtener datos actuales de un ETF/Fondo
     * GET /api/finnhub/etf/{symbol}
     * Ejemplo: GET /api/finnhub/etf/VWRL
     */
    @GetMapping("/etf/{symbol}")
    public ResponseEntity<?> getEtfData(@PathVariable String symbol) {
        try {
            String data = finnhubService.getEtfData(symbol);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Obtener datos históricos (velas) de un ETF/Fondo
     * GET /api/finnhub/candles/{symbol}/{resolution}/{days}
     * Resolution: 1, 5, 15, 30, 60 (minutos), D (día), W (semana), M (mes)
     * Ejemplo: GET /api/finnhub/candles/VWRL/D/30
     */
    @GetMapping("/candles/{symbol}/{resolution}/{days}")
    public ResponseEntity<?> getEtfCandles(
            @PathVariable String symbol,
            @PathVariable String resolution,
            @PathVariable int days) {
        try {
            String data = finnhubService.getEtfCandles(symbol, resolution, days);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Buscar ETF/Fondo por nombre
     * GET /api/finnhub/search/{query}
     * Ejemplo: GET /api/finnhub/search/VANGUARD
     */
    @GetMapping("/search/{query}")
    public ResponseEntity<?> searchEtf(@PathVariable String query) {
        try {
            String data = finnhubService.searchEtf(query);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
