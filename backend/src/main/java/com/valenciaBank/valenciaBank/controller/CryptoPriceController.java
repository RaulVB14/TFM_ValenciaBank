package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.model.CryptoPrice;
import com.valenciaBank.valenciaBank.repository.CryptoPriceRepository;
import com.valenciaBank.valenciaBank.service.CryptoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/crypto-prices")
public class CryptoPriceController {

    @Autowired
    private CryptoPriceRepository cryptoPriceRepository;

    @Autowired
    private CryptoService cryptoService;

    /**
     * Obtener todos los precios almacenados
     */
    @GetMapping
    public ResponseEntity<List<CryptoPrice>> getAllPrices() {
        return ResponseEntity.ok(cryptoPriceRepository.findAll());
    }

    /**
     * Obtener el precio de una criptomoneda en un mercado específico
     */
    @GetMapping("/{symbol}/{market}")
    public ResponseEntity<Map<String, Object>> getPrice(
            @PathVariable String symbol,
            @PathVariable String market) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<CryptoPrice> price = cryptoPriceRepository.findBySymbolAndMarket(symbol, market);
        if (price.isPresent()) {
            response.put("success", true);
            response.put("price", price.get().getPrice());
            response.put("lastUpdated", price.get().getLastUpdated());
            response.put("symbol", price.get().getSymbol());
            response.put("market", price.get().getMarket());
            return ResponseEntity.ok(response);
        }
        
        response.put("success", false);
        response.put("message", "Precio no encontrado para " + symbol + " en " + market);
        return ResponseEntity.notFound().build();
    }

    /**
     * Actualizar un precio existente o crear uno nuevo
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> updatePrice(
            @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String symbol = (String) request.get("symbol");
            String market = (String) request.get("market");
            Double price = Double.parseDouble(request.get("price").toString());

            Optional<CryptoPrice> existing = cryptoPriceRepository.findBySymbolAndMarket(symbol, market);
            
            CryptoPrice cryptoPrice;
            if (existing.isPresent()) {
                cryptoPrice = existing.get();
                cryptoPrice.setPrice(price);
            } else {
                cryptoPrice = new CryptoPrice(symbol, market, price);
            }
            
            CryptoPrice saved = cryptoPriceRepository.save(cryptoPrice);
            response.put("success", true);
            response.put("message", "Precio actualizado correctamente");
            response.put("data", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al actualizar precio: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Actualizar el precio de una criptomoneda desde la API de Alpha Vantage
     * POST /api/crypto-prices/update-from-api
     * Body: { "symbol": "BTC", "market": "EUR" }
     */
    @PostMapping("/update-from-api")
    public ResponseEntity<Map<String, Object>> updatePriceFromAPI(
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String symbol = request.get("symbol");
            String market = request.get("market");

            // Obtener precio de la API
            String jsonResponse = cryptoService.llamarAPIExterna(symbol, market);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            // Verificar si hay error
            if (root.has("error")) {
                response.put("success", false);
                response.put("message", root.get("error").asText());
                return ResponseEntity.badRequest().body(response);
            }

            Double price = null;

            // Intentar obtener de CURRENCY_EXCHANGE_RATE
            if (root.has("Realtime Currency Exchange Rate")) {
                JsonNode rate = root.get("Realtime Currency Exchange Rate");
                if (rate.has("5. Exchange Rate")) {
                    String priceStr = rate.get("5. Exchange Rate").asText();
                    price = Double.parseDouble(priceStr);
                }
            }

            // Intentar obtener de DIGITAL_CURRENCY_DAILY
            if (price == null && root.has("Time Series (Digital Currency Daily)")) {
                JsonNode timeSeries = root.get("Time Series (Digital Currency Daily)");
                Iterator<String> fieldNames = timeSeries.fieldNames();
                if (fieldNames.hasNext()) {
                    String latestDate = fieldNames.next();
                    JsonNode latestData = timeSeries.get(latestDate);
                    if (latestData.has("4. close")) {
                        String priceStr = latestData.get("4. close").asText();
                        price = Double.parseDouble(priceStr);
                    }
                }
            }

            if (price == null) {
                response.put("success", false);
                response.put("message", "No se pudo extraer el precio de la respuesta");
                return ResponseEntity.badRequest().body(response);
            }

            // Guardar en BD
            Optional<CryptoPrice> existing = cryptoPriceRepository.findBySymbolAndMarket(symbol, market);
            CryptoPrice cryptoPrice;
            if (existing.isPresent()) {
                cryptoPrice = existing.get();
                cryptoPrice.setPrice(price);
            } else {
                cryptoPrice = new CryptoPrice(symbol, market, price);
            }

            CryptoPrice saved = cryptoPriceRepository.save(cryptoPrice);
            response.put("success", true);
            response.put("message", "Precio actualizado desde API");
            response.put("data", saved);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Eliminar un precio
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePrice(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            cryptoPriceRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Precio eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
