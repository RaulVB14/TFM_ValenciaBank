package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.model.CryptoPurchase;
import com.valenciaBank.valenciaBank.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/portfolio")
@CrossOrigin(origins = "http://localhost:5173")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    // GET: Obtener todo el portfolio del usuario
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CryptoPurchase>> getPortfolio(@PathVariable Long userId) {
        try {
            List<CryptoPurchase> portfolio = portfolioService.getPortfolio(userId);
            return ResponseEntity.ok(portfolio);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET: Obtener holdings de una cripto específica
    @GetMapping("/user/{userId}/symbol/{symbol}")
    public ResponseEntity<List<CryptoPurchase>> getCryptoHoldings(
            @PathVariable Long userId,
            @PathVariable String symbol) {
        try {
            List<CryptoPurchase> holdings = portfolioService.getCryptoHoldings(userId, symbol);
            return ResponseEntity.ok(holdings);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET: Obtener cantidad total de una cripto
    @GetMapping("/user/{userId}/quantity/{symbol}")
    public ResponseEntity<Map<String, Object>> getTotalQuantity(
            @PathVariable Long userId,
            @PathVariable String symbol) {
        try {
            Double totalQuantity = portfolioService.getTotalQuantity(userId, symbol);
            Double averageCost = portfolioService.getAverageCost(userId, symbol);
            return ResponseEntity.ok(Map.of(
                    "symbol", symbol,
                    "totalQuantity", totalQuantity,
                    "averageCost", averageCost
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST: Agregar una compra de cripto
    @PostMapping("/add")
    public ResponseEntity<CryptoPurchase> addCryptoPurchase(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String symbol = request.get("symbol").toString();
            Double quantity = Double.parseDouble(request.get("quantity").toString());
            Double purchasePrice = Double.parseDouble(request.get("purchasePrice").toString());
            String currency = request.get("currency").toString();

            CryptoPurchase purchase = portfolioService.addCryptoPurchase(userId, symbol, quantity, purchasePrice, currency);
            return ResponseEntity.ok(purchase);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // DELETE: Eliminar una compra (venta completa)
    @DeleteMapping("/{purchaseId}")
    public ResponseEntity<Void> removeCryptoPurchase(@PathVariable Long purchaseId) {
        try {
            portfolioService.removeCryptoPurchase(purchaseId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT: Actualizar cantidad de una compra (venta parcial)
    @PutMapping("/{purchaseId}/quantity")
    public ResponseEntity<CryptoPurchase> updateQuantity(
            @PathVariable Long purchaseId,
            @RequestBody Map<String, Object> request) {
        try {
            Double newQuantity = Double.parseDouble(request.get("quantity").toString());
            CryptoPurchase updated = portfolioService.updateQuantity(purchaseId, newQuantity);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET: Obtener portafolio DETALLADO con precios actuales y ganancia/pérdida
    @GetMapping("/detailed/{userId}")
    public ResponseEntity<Map<String, Object>> getDetailedPortfolio(@PathVariable Long userId) {
        try {
            Map<String, Object> portfolio = portfolioService.getDetailedPortfolio(userId);
            return ResponseEntity.ok(portfolio);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Error al obtener portafolio: " + e.getMessage()
            ));
        }
    }

    // GET: Obtener historial de valor del portfolio para gráficos
    @GetMapping("/history/{userId}")
    public ResponseEntity<Map<String, Object>> getPortfolioHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        try {
            Map<String, Object> history = portfolioService.getPortfolioHistory(userId, days);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Error al obtener historial: " + e.getMessage()
            ));
        }
    }
}
