package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.model.FundPurchase;
import com.valenciaBank.valenciaBank.service.BuyFundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fund/purchase")
@CrossOrigin(origins = "http://localhost:5173")
public class BuyFundController {

    @Autowired
    private BuyFundService buyFundService;

    /**
     * POST: Comprar fondo/ETF
     * Body: {
     *   "userId": 1,
     *   "symbol": "SPY",
     *   "name": "S&P 500",
     *   "type": "ETF",
     *   "quantity": 2.5,
     *   "currency": "USD"
     * }
     */
    @PostMapping("/buy")
    public ResponseEntity<Map<String, Object>> buyFund(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String symbol = request.get("symbol").toString();
            String name = request.getOrDefault("name", symbol).toString();
            String type = request.getOrDefault("type", "ETF").toString();
            Double quantity = Double.parseDouble(request.get("quantity").toString());
            String currency = request.getOrDefault("currency", "USD").toString();

            Map<String, Object> result = buyFundService.buyFund(userId, symbol, name, type, quantity, currency);

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
     * POST: Vender fondo/ETF
     * Body: {
     *   "userId": 1,
     *   "purchaseId": 5,
     *   "quantityToSell": 1.0
     * }
     */
    @PostMapping("/sell")
    public ResponseEntity<Map<String, Object>> sellFund(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            Long purchaseId = Long.parseLong(request.get("purchaseId").toString());
            Double quantityToSell = Double.parseDouble(request.get("quantityToSell").toString());

            Map<String, Object> result = buyFundService.sellFund(userId, purchaseId, quantityToSell);

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
     * GET: Obtener portfolio de fondos del usuario
     */
    @GetMapping("/portfolio/{userId}")
    public ResponseEntity<?> getUserFunds(@PathVariable Long userId) {
        try {
            List<FundPurchase> funds = buyFundService.getUserFunds(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "funds", funds
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * GET: Obtener portfolio detallado de fondos con precios actuales
     */
    @GetMapping("/portfolio/detailed/{userId}")
    public ResponseEntity<?> getDetailedFundPortfolio(@PathVariable Long userId) {
        try {
            Map<String, Object> result = buyFundService.getDetailedFundPortfolio(userId);
            boolean success = (boolean) result.get("success");
            return success ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}
