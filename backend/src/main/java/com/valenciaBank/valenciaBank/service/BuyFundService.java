package com.valenciaBank.valenciaBank.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valenciaBank.valenciaBank.model.Account;
import com.valenciaBank.valenciaBank.model.FundPurchase;
import com.valenciaBank.valenciaBank.model.User;
import com.valenciaBank.valenciaBank.repository.AccountRepository;
import com.valenciaBank.valenciaBank.repository.FundPurchaseRepository;
import com.valenciaBank.valenciaBank.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BuyFundService {

    private static final Logger log = LoggerFactory.getLogger(BuyFundService.class);

    @Autowired
    private FinnhubService finnhubService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private FundPurchaseRepository fundPurchaseRepository;

    /**
     * Compra un fondo/ETF si el usuario tiene saldo suficiente
     */
    @Transactional
    public Map<String, Object> buyFund(Long userId, String symbol, String name, String type, Double quantity, String currency) {
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

            // 2. Obtener precio actual del fondo/ETF desde Finnhub
            Double currentPrice = getCurrentFundPrice(symbol);
            if (currentPrice == null || currentPrice <= 0) {
                throw new RuntimeException("No se pudo obtener el precio actual de " + symbol);
            }

            // 3. Calcular costo total
            Double totalCost = quantity * currentPrice;

            // 4. Verificar saldo suficiente
            if (account.getBalance() < totalCost) {
                throw new RuntimeException("Saldo insuficiente. Necesitas " + String.format("%.2f", totalCost) +
                    " " + currency + " pero solo tienes " + String.format("%.2f", account.getBalance()) + " EUR");
            }

            // 5. Restar dinero de la cuenta
            Double newBalance = account.getBalance() - totalCost;
            account.setBalance(newBalance);
            accountRepository.save(account);

            // 6. Registrar la compra
            FundPurchase purchase = new FundPurchase(
                user.get(), symbol, name, type, quantity, currentPrice, currency
            );
            fundPurchaseRepository.save(purchase);

            // 7. Retornar información de la compra
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("message", "Compra realizada exitosamente");
            result.put("symbol", symbol);
            result.put("name", name);
            result.put("type", type);
            result.put("quantity", quantity);
            result.put("pricePerUnit", currentPrice);
            result.put("totalCost", totalCost);
            result.put("currency", currency);
            result.put("newBalance", newBalance);
            result.put("purchaseId", purchase.getId());
            result.put("purchaseDate", purchase.getPurchaseDate().toString());
            return result;

        } catch (RuntimeException e) {
            return Map.of("success", false, "error", e.getMessage());
        } catch (Exception e) {
            return Map.of("success", false, "error", "Error al procesar la compra: " + e.getMessage());
        }
    }

    /**
     * Vender fondo/ETF
     */
    @Transactional
    public Map<String, Object> sellFund(Long userId, Long purchaseId, Double quantityToSell) {
        try {
            Optional<FundPurchase> purchase = fundPurchaseRepository.findById(purchaseId);
            if (purchase.isEmpty()) {
                throw new RuntimeException("Compra no encontrada");
            }

            FundPurchase p = purchase.get();

            if (!p.getUser().getId().equals(userId)) {
                throw new RuntimeException("Esta compra no pertenece al usuario");
            }

            if (p.getQuantity() < quantityToSell) {
                throw new RuntimeException("Cantidad insuficiente. Tienes " + p.getQuantity() +
                    " pero quieres vender " + quantityToSell);
            }

            Double currentPrice = getCurrentFundPrice(p.getSymbol());
            if (currentPrice == null) {
                throw new RuntimeException("No se pudo obtener el precio actual de " + p.getSymbol());
            }

            Double saleAmount = quantityToSell * currentPrice;

            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                throw new RuntimeException("Usuario no encontrado");
            }

            Account account = user.get().getAccount();
            Double newBalance = account.getBalance() + saleAmount;
            account.setBalance(newBalance);
            accountRepository.save(account);

            if (quantityToSell.equals(p.getQuantity())) {
                fundPurchaseRepository.deleteById(purchaseId);
            } else {
                p.setQuantity(p.getQuantity() - quantityToSell);
                fundPurchaseRepository.save(p);
            }

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
            return Map.of("success", false, "error", e.getMessage());
        } catch (Exception e) {
            return Map.of("success", false, "error", "Error al procesar la venta: " + e.getMessage());
        }
    }

    /**
     * Obtener el portfolio de fondos de un usuario
     */
    public List<FundPurchase> getUserFunds(Long userId) {
        return fundPurchaseRepository.findByUserId(userId);
    }

    /**
     * Obtener portafolio detallado de fondos con precios actuales.
     * Agrupa por símbolo y calcula ganancia/pérdida.
     */
    public Map<String, Object> getDetailedFundPortfolio(Long userId) {
        try {
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                throw new RuntimeException("Usuario no encontrado");
            }

            List<FundPurchase> funds = getUserFunds(userId);

            // Agrupar por símbolo
            Map<String, List<FundPurchase>> groupedBySymbol = funds.stream()
                    .collect(Collectors.groupingBy(FundPurchase::getSymbol));

            List<Map<String, Object>> positions = new ArrayList<>();
            double totalInvested = 0.0;
            double totalCurrentValue = 0.0;

            for (Map.Entry<String, List<FundPurchase>> entry : groupedBySymbol.entrySet()) {
                String symbol = entry.getKey();
                List<FundPurchase> holdings = entry.getValue();

                double totalQuantity = holdings.stream()
                        .mapToDouble(FundPurchase::getQuantity)
                        .sum();

                double totalCostBasis = holdings.stream()
                        .mapToDouble(p -> p.getQuantity() * p.getPurchasePrice())
                        .sum();

                double averagePrice = totalCostBasis / totalQuantity;

                // Obtener precio actual desde Finnhub
                Double currentPrice = getCurrentFundPrice(symbol);
                if (currentPrice == null || currentPrice <= 0) {
                    currentPrice = averagePrice; // fallback
                }

                double currentValue = totalQuantity * currentPrice;
                double gainLoss = currentValue - totalCostBasis;
                double gainLossPercent = totalCostBasis > 0 ? (gainLoss / totalCostBasis) * 100 : 0.0;

                // Datos del primer holding para nombre y tipo
                String name = holdings.get(0).getName();
                String type = holdings.get(0).getType();
                String currency = holdings.get(0).getCurrency();

                Map<String, Object> position = new LinkedHashMap<>();
                position.put("symbol", symbol);
                position.put("name", name);
                position.put("type", type);
                position.put("quantity", totalQuantity);
                position.put("averagePrice", averagePrice);
                position.put("currentPrice", currentPrice);
                position.put("investmentValue", totalCostBasis);
                position.put("currentValue", currentValue);
                position.put("gainLoss", gainLoss);
                position.put("gainLossPercent", gainLossPercent);
                position.put("currency", currency);
                position.put("firstPurchaseDate", holdings.stream()
                        .min(Comparator.comparing(FundPurchase::getPurchaseDate))
                        .get()
                        .getPurchaseDate()
                        .toString());

                positions.add(position);
                totalInvested += totalCostBasis;
                totalCurrentValue += currentValue;
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("positions", positions);

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("totalInvested", totalInvested);
            summary.put("totalCurrentValue", totalCurrentValue);
            summary.put("totalGainLoss", totalCurrentValue - totalInvested);
            summary.put("totalGainLossPercent", totalInvested > 0 ? ((totalCurrentValue - totalInvested) / totalInvested) * 100 : 0.0);
            response.put("summary", summary);

            return response;
        } catch (Exception e) {
            log.error("Error al obtener portafolio detallado de fondos: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "error", "Error al obtener portafolio de fondos: " + e.getMessage()
            );
        }
    }

    /**
     * Obtener precio actual de un fondo/ETF desde Finnhub
     */
    private Double getCurrentFundPrice(String symbol) {
        try {
            String jsonResponse = finnhubService.getEtfData(symbol);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            if (root.has("error")) {
                log.error("Error de Finnhub: {}", root.get("error").asText());
                return null;
            }

            // Finnhub quote devuelve: { "c": currentPrice, "h": high, "l": low, "o": open, "pc": previousClose }
            if (root.has("c")) {
                Double price = root.get("c").asDouble();
                if (price > 0) {
                    log.info("Precio actual de {}: {} USD", symbol, price);
                    return price;
                }
            }

            log.warn("No se encontró precio para {}", symbol);
            return null;
        } catch (Exception e) {
            log.error("Error al obtener precio de {}: {}", symbol, e.getMessage());
            return null;
        }
    }
}
