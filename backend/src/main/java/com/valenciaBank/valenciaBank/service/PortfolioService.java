package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.CryptoPurchase;
import com.valenciaBank.valenciaBank.model.CryptoPrice;
import com.valenciaBank.valenciaBank.model.User;
import com.valenciaBank.valenciaBank.repository.CryptoPriceRepository;
import com.valenciaBank.valenciaBank.repository.CryptoPurchaseRepository;
import com.valenciaBank.valenciaBank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    @Autowired
    private CryptoPurchaseRepository cryptoPurchaseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoinGeckoService coinGeckoService;

    @Autowired
    private CryptoPriceRepository cryptoPriceRepository;

    // Agregar una compra de cripto al portfolio
    public CryptoPurchase addCryptoPurchase(Long userId, String symbol, Double quantity, Double purchasePrice, String currency) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        CryptoPurchase purchase = new CryptoPurchase(user.get(), symbol, quantity, purchasePrice, currency);
        return cryptoPurchaseRepository.save(purchase);
    }

    // Obtener todo el portfolio de un usuario
    public List<CryptoPurchase> getPortfolio(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }
        return cryptoPurchaseRepository.findByUser(user.get());
    }

    // Obtener todas las compras de una cripto específica
    public List<CryptoPurchase> getCryptoHoldings(Long userId, String symbol) {
        return cryptoPurchaseRepository.findByUserIdAndSymbol(userId, symbol);
    }

    // Calcular la cantidad total de una cripto que posee el usuario
    public Double getTotalQuantity(Long userId, String symbol) {
        List<CryptoPurchase> purchases = getCryptoHoldings(userId, symbol);
        return purchases.stream()
                .mapToDouble(CryptoPurchase::getQuantity)
                .sum();
    }

    // Calcular el costo promedio de una cripto
    public Double getAverageCost(Long userId, String symbol) {
        List<CryptoPurchase> purchases = getCryptoHoldings(userId, symbol);
        if (purchases.isEmpty()) {
            return 0.0;
        }

        Double totalCost = purchases.stream()
                .mapToDouble(p -> p.getQuantity() * p.getPurchasePrice())
                .sum();
        Double totalQuantity = getTotalQuantity(userId, symbol);

        return totalCost / totalQuantity;
    }

    // Eliminar una compra (venta de cripto)
    public void removeCryptoPurchase(Long purchaseId) {
        if (cryptoPurchaseRepository.existsById(purchaseId)) {
            cryptoPurchaseRepository.deleteById(purchaseId);
        } else {
            throw new RuntimeException("Compra no encontrada");
        }
    }

    // Actualizar cantidad de una compra (parcialmente vendida)
    public CryptoPurchase updateQuantity(Long purchaseId, Double newQuantity) {
        Optional<CryptoPurchase> purchase = cryptoPurchaseRepository.findById(purchaseId);
        if (purchase.isEmpty()) {
            throw new RuntimeException("Compra no encontrada");
        }

        CryptoPurchase p = purchase.get();
        if (newQuantity <= 0) {
            cryptoPurchaseRepository.deleteById(purchaseId);
            return null;
        }

        p.setQuantity(newQuantity);
        return cryptoPurchaseRepository.save(p);
    }

    /**
     * Obtener portafolio detallado con precios actuales
     * Agrupa por símbolo y calcula ganancia/pérdida
     */
    public Map<String, Object> getDetailedPortfolio(Long userId) {
        try {
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                throw new RuntimeException("Usuario no encontrado");
            }

            List<CryptoPurchase> portfolio = getPortfolio(userId);
            
            // Agrupar por símbolo
            Map<String, List<CryptoPurchase>> groupedBySymbol = portfolio.stream()
                    .collect(Collectors.groupingBy(CryptoPurchase::getSymbol));

            // Construir lista de posiciones
            List<Map<String, Object>> positions = new ArrayList<>();
            Double totalInvested = 0.0;
            Double totalCurrentValue = 0.0;

            for (Map.Entry<String, List<CryptoPurchase>> entry : groupedBySymbol.entrySet()) {
                String symbol = entry.getKey();
                List<CryptoPurchase> holdings = entry.getValue();

                // Calcular totales
                Double totalQuantity = holdings.stream()
                        .mapToDouble(CryptoPurchase::getQuantity)
                        .sum();
                
                Double totalCostBasis = holdings.stream()
                        .mapToDouble(p -> p.getQuantity() * p.getPurchasePrice())
                        .sum();
                
                Double averagePrice = totalCostBasis / totalQuantity;

                // Obtener precio actual
                Double currentPrice = getCurrentPriceForSymbol(symbol, "EUR");
                Double currentValue = totalQuantity * currentPrice;
                Double gainLoss = currentValue - totalCostBasis;
                Double gainLossPercent = (gainLoss / totalCostBasis) * 100;

                // Construir objeto de posición
                Map<String, Object> position = new LinkedHashMap<>();
                position.put("symbol", symbol);
                position.put("quantity", totalQuantity);
                position.put("averagePrice", averagePrice);
                position.put("currentPrice", currentPrice);
                position.put("investmentValue", totalCostBasis);
                position.put("currentValue", currentValue);
                position.put("gainLoss", gainLoss);
                position.put("gainLossPercent", gainLossPercent);
                position.put("firstPurchaseDate", holdings.stream()
                        .min(Comparator.comparing(CryptoPurchase::getPurchaseDate))
                        .get()
                        .getPurchaseDate()
                        .toString()); // Convertir a String para serialización JSON

                positions.add(position);
                totalInvested += totalCostBasis;
                totalCurrentValue += currentValue;
            }

            // Construir respuesta
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("positions", positions);
            response.put("summary", Map.of(
                    "totalInvested", totalInvested,
                    "totalCurrentValue", totalCurrentValue,
                    "totalGainLoss", totalCurrentValue - totalInvested,
                    "totalGainLossPercent", totalInvested > 0 ? ((totalCurrentValue - totalInvested) / totalInvested) * 100 : 0.0
            ));

            return response;
        } catch (Exception e) {
            System.err.println("Error al obtener portafolio detallado: " + e.getMessage());
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "error", "Error al obtener portafolio: " + e.getMessage()
            );
        }
    }

    /**
     * Obtener precio actual de un símbolo (helper privado)
     * Primero intenta CoinGecko, si falla busca en BD
     */
    private Double getCurrentPriceForSymbol(String symbol, String market) {
        try {
            // Intenta obtener de CoinGecko
            String jsonResponse = coinGeckoService.getCryptoData(symbol, market);
            String coinGeckoId = coinGeckoService.convertSymbolToCoinGeckoId(symbol);
            
            // Parsear JSON manualmente
            if (jsonResponse.contains(coinGeckoId) && jsonResponse.contains(market.toLowerCase())) {
                // Búsqueda simple: "bitcoin":{"eur":60000}
                int idIndex = jsonResponse.indexOf("\"" + coinGeckoId + "\"");
                int currencyIndex = jsonResponse.indexOf("\"" + market.toLowerCase() + "\"", idIndex);
                int colonIndex = jsonResponse.indexOf(":", currencyIndex);
                int commaIndex = jsonResponse.indexOf(",", colonIndex);
                int braceIndex = jsonResponse.indexOf("}", colonIndex);
                
                int endIndex = commaIndex > 0 && commaIndex < braceIndex ? commaIndex : braceIndex;
                String priceStr = jsonResponse.substring(colonIndex + 1, endIndex).trim();
                
                Double price = Double.parseDouble(priceStr);
                if (price > 0) {
                    return price;
                }
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo precio de CoinGecko para " + symbol + ": " + e.getMessage());
        }
        
        // Fallback: buscar en base de datos
        try {
            Optional<CryptoPrice> dbPrice = cryptoPriceRepository.findBySymbolAndMarket(symbol, market);
            if (dbPrice.isPresent()) {
                Double price = dbPrice.get().getPrice();
                System.out.println("Usando precio de BD para " + symbol + ": " + price + " " + market);
                return price;
            }
        } catch (Exception e) {
            System.err.println("Error buscando precio en BD para " + symbol + ": " + e.getMessage());
        }
        
        return 0.0;
    }}