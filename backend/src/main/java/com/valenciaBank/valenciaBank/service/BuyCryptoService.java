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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Service
public class BuyCryptoService {

    private static final Logger log = LoggerFactory.getLogger(BuyCryptoService.class);

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

    @Autowired
    private CoinGeckoService coinGeckoService;

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
     * Obtiene el precio actual de una criptomoneda
     * Siempre intenta obtener el precio ACTUAL de CoinGecko
     * Si falla, usa el precio en caché de BD como fallback
     */
    private Double getCurrentPrice(String symbol, String market) {
        try {
            // 1. PRIORIDAD: Obtener precio actual de CoinGecko
            log.info("Obteniendo precio ACTUAL de CoinGecko para {} en {}...", symbol, market);
            try {
                String jsonResponse = coinGeckoService.getCryptoData(symbol, market);
                log.debug("Respuesta de CoinGecko: {}", jsonResponse);
                
                // Parsear respuesta JSON de CoinGecko
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(jsonResponse);
                
                // Validar que no hay error
                if (root.has("error")) {
                    log.error("Error en respuesta de CoinGecko: {}", root.get("error").asText());
                    throw new Exception("API error: " + root.get("error").asText());
                }
                
                // CoinGecko devuelve: { "bitcoin": { "eur": 60000 } }
                // Buscar la clave correcta del símbolo
                String coinGeckoId = coinGeckoService.convertSymbolToCoinGeckoId(symbol);
                log.debug("Buscando ID de CoinGecko: {}", coinGeckoId);
                
                if (root.has(coinGeckoId)) {
                    JsonNode cryptoData = root.get(coinGeckoId);
                    String currencyKey = market.toLowerCase();
                    
                    if (cryptoData.has(currencyKey)) {
                        Double price = cryptoData.get(currencyKey).asDouble();
                        log.info("Precio actual obtenido de CoinGecko: {} = {} {}", symbol, price, market);
                        
                        // Guardar o actualizar en BD (usar findAndUpdate, no save directo)
                        Optional<CryptoPrice> existingPrice = cryptoPriceRepository.findBySymbolAndMarket(symbol, market);
                        if (existingPrice.isPresent()) {
                            // Actualizar precio existente
                            CryptoPrice existing = existingPrice.get();
                            existing.setPrice(price);
                            cryptoPriceRepository.save(existing);
                            log.info("Precio actualizado en BD para {}", symbol);
                        } else {
                            // Crear nuevo registro
                            CryptoPrice cryptoPrice = new CryptoPrice(symbol, market, price);
                            cryptoPriceRepository.save(cryptoPrice);
                            log.info("Nuevo precio guardado en BD para {}", symbol);
                        }
                        
                        return price;
                    } else {
                        log.error("No encontrada moneda '{}' en respuesta. Keys disponibles: {}", currencyKey, cryptoData.fieldNames().next());
                    }
                } else {
                    log.error("No encontrado símbolo '{}' en respuesta. Keys disponibles: {}", coinGeckoId, root.fieldNames().next());
                }
            } catch (Exception e) {
                log.warn("No se pudo obtener precio de CoinGecko: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
                // Continuar con fallback a BD
            }

            // 2. FALLBACK: Si CoinGecko falla, usar precio en caché de BD
            log.info("Usando precio en caché de BD como fallback...");
            Optional<CryptoPrice> dbPrice = cryptoPriceRepository.findBySymbolAndMarket(symbol, market);
            if (dbPrice.isPresent()) {
                Double cachedPrice = dbPrice.get().getPrice();
                log.warn("Usando precio en caché (puede estar desactualizado): {} = {}", symbol, cachedPrice);
                return cachedPrice;
            }

            log.error("No se encontró precio ni en CoinGecko ni en BD");
            return null;
            
        } catch (Exception e) {
            log.error("Error crítico al obtener precio: {}", e.getMessage(), e);
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
