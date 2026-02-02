package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.service.BuyCryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/crypto/purchase")
@CrossOrigin(origins = "http://localhost:5173")
public class BuyCryptoController {

    @Autowired
    private BuyCryptoService buyCryptoService;

    /**
     * POST: Comprar criptomoneda
     * Body: {
     *   "userId": 1,
     *   "symbol": "BTC",
     *   "quantity": 0.5,
     *   "market": "EUR"
     * }
     */
    @PostMapping("/buy")
    public ResponseEntity<Map<String, Object>> buyCrypto(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String symbol = request.get("symbol").toString();
            Double quantity = Double.parseDouble(request.get("quantity").toString());
            String market = request.getOrDefault("market", "EUR").toString();

            Map<String, Object> result = buyCryptoService.buyCrypto(userId, symbol, quantity, market);
            
            boolean success = (boolean) result.get("success");
            return success ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Formato de solicitud inválido: " + e.getMessage()
            ));
        }
    }

    /**
     * POST: Vender criptomoneda
     * Body: {
     *   "userId": 1,
     *   "purchaseId": 5,
     *   "quantityToSell": 0.25
     * }
     */
    @PostMapping("/sell")
    public ResponseEntity<Map<String, Object>> sellCrypto(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            Long purchaseId = Long.parseLong(request.get("purchaseId").toString());
            Double quantityToSell = Double.parseDouble(request.get("quantityToSell").toString());

            Map<String, Object> result = buyCryptoService.sellCrypto(userId, purchaseId, quantityToSell);
            
            boolean success = (boolean) result.get("success");
            return success ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Formato de solicitud inválido: " + e.getMessage()
            ));
        }
    }
}
