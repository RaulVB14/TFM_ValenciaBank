package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crypto")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "*"})
public class API_InversionesCryptoController {

    @Autowired
    private CryptoService cryptoService;

    /**
     * ✅ GET - Datos históricos diarios (Alpha Vantage)
     * URL: GET /api/crypto/daily?symbol=BTC&market=EUR
     */
    @GetMapping("/daily")
    public ResponseEntity<String> getDigitalCurrencyDaily(
            @RequestParam String symbol, 
            @RequestParam(defaultValue = "EUR") String market) {
        String data = cryptoService.llamarAPI(symbol, market);
        return ResponseEntity.ok(data);
    }

    /**
     * ✅ GET - Precio actual (CoinGecko - sin límite de rate)
     * URL: GET /api/crypto/current/bitcoin
     */
    @GetMapping("/current/{symbol}")
    public ResponseEntity<String> getCurrentPrice(@PathVariable String symbol) {
        String data = cryptoService.getCurrentPrice(symbol);
        return ResponseEntity.ok(data);
    }

    /**
     * ✅ GET - Top 10 criptomonedas por market cap
     * URL: GET /api/crypto/top10
     */
    @GetMapping("/top10")
    public ResponseEntity<String> getTop10Cryptocurrencies() {
        String data = cryptoService.getTop10Cryptocurrencies();
        return ResponseEntity.ok(data);
    }

    /**
     * ✅ GET - Análisis de una criptomoneda (promedio, volatilidad, cambio %)
     * URL: GET /api/crypto/analysis/BTC?days=30
     */
    @GetMapping("/analysis/{symbol}")
    public ResponseEntity<String> getAnalysis(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "30") int days) {
        String data = cryptoService.getAnalysis(symbol, days);
        return ResponseEntity.ok(data);
    }

    /**
     * ✅ GET - Historial filtrado por rango de fechas
     * URL: GET /api/crypto/history/BTC?startDate=2026-01-01&endDate=2026-01-30
     */
    @GetMapping("/history/{symbol}")
    public ResponseEntity<String> getHistoryByDateRange(
            @PathVariable String symbol,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        String data = cryptoService.getHistoryByDateRange(symbol, startDate, endDate);
        return ResponseEntity.ok(data);
    }

    /**
     * ✅ GET - Comparar múltiples criptomonedas
     * URL: GET /api/crypto/compare?symbols=BTC,ETH,XRP
     */
    @GetMapping("/compare")
    public ResponseEntity<String> compareCryptos(@RequestParam String symbols) {
        String data = cryptoService.compareCryptos(symbols);
        return ResponseEntity.ok(data);
    }

    /**
     * 📦 Endpoint antiguo (compatibilidad hacia atrás)
     * URL: GET /api/crypto/digitalCurrencyDaily?symbol=BTC&market=EUR
     */
    @GetMapping("/digitalCurrencyDaily")
    public ResponseEntity<String> getDigitalCurrencyDailyLegacy(
            @RequestParam String symbol,
            @RequestParam String market) {
        String data = cryptoService.llamarAPI(symbol, market);
        return ResponseEntity.ok(data);
    }

    /**
     * ✅ POST - Subir datos manualmente
     * URL: POST /api/crypto/datos
     * Body: JSON con "Time Series (Digital Currency Daily)"
     */
    @PostMapping("/datos")
    public ResponseEntity<String> recibirJson(@RequestBody String json) {
        try {
            cryptoService.saveDataFromManualJson(json);
            return ResponseEntity.ok("{\"message\":\"Datos guardados correctamente\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("{\"error\":\"Error al procesar el JSON: " + e.getMessage() + "\"}");
        }
    }
}