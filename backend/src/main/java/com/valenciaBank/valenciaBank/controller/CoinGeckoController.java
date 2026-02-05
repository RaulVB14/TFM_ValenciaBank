package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.service.CoinGeckoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coingecko")
public class CoinGeckoController {

    @Autowired
    private CoinGeckoService coinGeckoService;

    /**
     * Obtener precio actual de una criptomoneda
     * GET /api/coingecko/price/{symbol}/{currency}
     * Ejemplo: GET /api/coingecko/price/BTC/EUR
     */
    @GetMapping("/price/{symbol}/{currency}")
    public ResponseEntity<?> getCryptoPrice(
            @PathVariable String symbol,
            @PathVariable String currency) {
        try {
            String data = coinGeckoService.getCryptoData(symbol, currency);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Obtener datos históricos de una criptomoneda
     * GET /api/coingecko/history/{symbol}/{days}/{currency}
     * Ejemplo: GET /api/coingecko/history/BTC/30/EUR
     */
    @GetMapping("/history/{symbol}/{days}/{currency}")
    public ResponseEntity<?> getCryptoHistory(
            @PathVariable String symbol,
            @PathVariable int days,
            @PathVariable String currency) {
        try {
            String data = coinGeckoService.getCryptoHistory(symbol, days, currency);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
