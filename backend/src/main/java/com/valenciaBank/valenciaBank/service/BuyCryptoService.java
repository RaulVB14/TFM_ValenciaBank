package com.valenciaBank.valenciaBank.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valenciaBank.valenciaBank.model.Account;
import com.valenciaBank.valenciaBank.model.CryptoPurchase;
import com.valenciaBank.valenciaBank.model.CryptoPrice;
import com.valenciaBank.valenciaBank.model.User;
import com.valenciaBank.valenciaBank.repository.AccountRepository;
import com.valenciaBank.valenciaBank.repository.CryptoPriceRepository;
import com.valenciaBank.valenciaBank.repository.CryptoPurchaseRepository;
import com.valenciaBank.valenciaBank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Service
public class BuyCryptoService {

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CryptoPurchaseRepository cryptoPurchaseRepository;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private CryptoPriceRepository cryptoPriceRepository;

    /**
     * Compra una criptomoneda si el usuario tiene saldo suficiente
     * @param userId ID del usuario
     * @param symbol Símbolo de la cripto (BTC, ETH, etc)
     * @param quantity Cantidad a comprar
     * @param market Moneda de mercado (EUR, USD)
     * @return Información de la compra realizada
     */
    @Transactional
    public Map<String, Object> buyCrypto(Long userId, String symbol, Double quantity, String market) {
        try {
            // 1. Obtener usuario y cuenta
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                throw new RuntimeException("Usuario no encontrado");
            }

            Account account = user.get().getAccount();
            if (account == null) {
                throw new RuntimeException("Cuenta del usuario no encontrada");
            }

            // 2. Obtener precio actual de la cripto
            Double currentPrice = getCurrentPrice(symbol, market);
            if (currentPrice == null || currentPrice <= 0) {
                throw new RuntimeException("No se pudo obtener el precio actual de " + symbol);
            }

            // 3. Calcular costo total
            Double totalCost = quantity * currentPrice;

            // 4. Verificar saldo suficiente
            if (account.getBalance() < totalCost) {
                throw new RuntimeException("Saldo insuficiente. Necesitas " + totalCost + 
                    " EUR pero solo tienes " + account.getBalance() + " EUR");
            }

            // 5. Restar dinero de la cuenta
            Double newBalance = account.getBalance() - totalCost;
            account.setBalance(newBalance);
            accountRepository.save(account);

            // 6. Registrar la compra en el portfolio
            CryptoPurchase purchase = portfolioService.addCryptoPurchase(userId, symbol, quantity, currentPrice, market);

            // 7. Retornar información de la compra
            return Map.of(
                    "success", true,
                    "message", "Compra realizada exitosamente",
                    "symbol", symbol,
                    "quantity", quantity,
                    "pricePerUnit", currentPrice,
                    "totalCost", totalCost,
                    "newBalance", newBalance,
                    "purchaseId", purchase.getId(),
                    "purchaseDate", purchase.getPurchaseDate().toString()
            );

        } catch (RuntimeException e) {
            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "error", "Error al procesar la compra: " + e.getMessage()
            );
        }
    }

    /**
     * Obtiene el precio actual de una criptomoneda desde la BD local
     * Si no existe, intenta obtenerlo de la API y lo almacena
     */
    private Double getCurrentPrice(String symbol, String market) {
        try {
            // 1. Buscar primero en la BD local
            Optional<CryptoPrice> dbPrice = cryptoPriceRepository.findBySymbolAndMarket(symbol, market);
            if (dbPrice.isPresent()) {
                System.out.println("Precio obtenido de BD local: " + symbol + " = " + dbPrice.get().getPrice());
                return dbPrice.get().getPrice();
            }

            // 2. Si no está en BD, intentar obtener de la API
            System.out.println("Precio no encontrado en BD, intentando obtener de API...");
            String jsonResponse = cryptoService.llamarAPIExterna(symbol, market);
            
            // 3. Parsear la respuesta JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            // Verificar si hay error de API
            if (root.has("error")) {
                String errorMsg = root.get("error").asText();
                System.err.println("Error de API: " + errorMsg);
                // Retornar null para que el servicio maneje el error
                return null;
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

            // Intentar obtener de DIGITAL_CURRENCY_DAILY (Time Series)
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

            // 4. Si se obtuvo el precio, guardarlo en BD
            if (price != null) {
                CryptoPrice cryptoPrice = new CryptoPrice(symbol, market, price);
                cryptoPriceRepository.save(cryptoPrice);
                System.out.println("Precio guardado en BD: " + symbol + " = " + price);
                return price;
            }

            System.err.println("No se pudo extraer el precio de la respuesta de API");
            return null;
            
        } catch (Exception e) {
            System.err.println("Error al obtener precio: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Vender criptos (eliminar del portfolio y devolver dinero a la cuenta)
     */
    @Transactional
    public Map<String, Object> sellCrypto(Long userId, Long purchaseId, Double quantityToSell) {
        try {
            // Obtener la compra
            Optional<CryptoPurchase> purchase = cryptoPurchaseRepository.findById(purchaseId);
            if (purchase.isEmpty()) {
                throw new RuntimeException("Compra no encontrada");
            }

            CryptoPurchase p = purchase.get();
            
            // Verificar que pertenece al usuario
            if (!p.getUser().getId().equals(userId)) {
                throw new RuntimeException("Esta compra no pertenece al usuario");
            }

            // Verificar cantidad disponible
            if (p.getQuantity() < quantityToSell) {
                throw new RuntimeException("Cantidad insuficiente. Tienes " + p.getQuantity() + 
                    " pero quieres vender " + quantityToSell);
            }

            // Obtener precio actual para calcular ganancia/pérdida
            Double currentPrice = getCurrentPrice(p.getSymbol(), p.getCurrency());
            if (currentPrice == null) {
                throw new RuntimeException("No se pudo obtener el precio actual de " + p.getSymbol());
            }

            // Calcular dinero a devolver
            Double saleAmount = quantityToSell * currentPrice;

            // Actualizar balance de la cuenta
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                throw new RuntimeException("Usuario no encontrado");
            }

            Account account = user.get().getAccount();
            Double newBalance = account.getBalance() + saleAmount;
            account.setBalance(newBalance);
            accountRepository.save(account);

            // Actualizar o eliminar la compra
            if (quantityToSell.equals(p.getQuantity())) {
                // Vender todo
                cryptoPurchaseRepository.deleteById(purchaseId);
            } else {
                // Vender parcialmente
                p.setQuantity(p.getQuantity() - quantityToSell);
                cryptoPurchaseRepository.save(p);
            }

            // Calcular ganancia/pérdida
            Double originalCost = quantityToSell * p.getPurchasePrice();
            Double profit = saleAmount - originalCost;
            Double profitPercent = (profit / originalCost) * 100;

            return Map.of(
                    "success", true,
                    "message", "Venta realizada exitosamente",
                    "symbol", p.getSymbol(),
                    "quantitySold", quantityToSell,
                    "sellPrice", currentPrice,
                    "totalSale", saleAmount,
                    "originalCost", originalCost,
                    "profit", profit,
                    "profitPercent", profitPercent,
                    "newBalance", newBalance
            );

        } catch (RuntimeException e) {
            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "error", "Error al procesar la venta: " + e.getMessage()
            );
        }
    }
}
