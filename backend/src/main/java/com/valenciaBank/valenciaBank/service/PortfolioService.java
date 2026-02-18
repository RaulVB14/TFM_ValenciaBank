package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.CryptoPurchase;
import com.valenciaBank.valenciaBank.model.CryptoPrice;
import com.valenciaBank.valenciaBank.model.User;
import com.valenciaBank.valenciaBank.repository.CryptoPriceRepository;
import com.valenciaBank.valenciaBank.repository.CryptoPurchaseRepository;
import com.valenciaBank.valenciaBank.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioService.class);

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
            log.error("Error al obtener portafolio detallado: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "error", "Error al obtener portafolio: " + e.getMessage()
            );
        }
    }

    /**
     * Obtener precio actual de un símbolo (helper privado)
     * Primero intenta CoinGecko, si falla busca en BD
     * Siempre guarda el precio más reciente en BD para mantener el fallback actualizado
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
                    // Guardar/actualizar precio en BD para mantener fallback siempre fresco
                    try {
                        Optional<CryptoPrice> existing = cryptoPriceRepository.findBySymbolAndMarket(symbol.toUpperCase(), market.toUpperCase());
                        if (existing.isPresent()) {
                            CryptoPrice cp = existing.get();
                            cp.setPrice(price);
                            cp.setLastUpdated(java.time.LocalDateTime.now());
                            cryptoPriceRepository.save(cp);
                        } else {
                            CryptoPrice cp = new CryptoPrice(symbol.toUpperCase(), market.toUpperCase(), price);
                            cryptoPriceRepository.save(cp);
                        }
                        log.info("Precio actualizado en BD para {}: {} {}", symbol, price, market);
                    } catch (Exception dbErr) {
                        log.warn("No se pudo guardar precio en BD: {}", dbErr.getMessage());
                    }
                    return price;
                }
            }
        } catch (Exception e) {
            log.error("Error obteniendo precio de CoinGecko para {}: {}", symbol, e.getMessage());
        }
        
        // Fallback: buscar en base de datos
        try {
            Optional<CryptoPrice> dbPrice = cryptoPriceRepository.findBySymbolAndMarket(symbol.toUpperCase(), market.toUpperCase());
            if (dbPrice.isPresent()) {
                Double price = dbPrice.get().getPrice();
                log.info("Usando precio de BD para {}: {} {} (último update: {})", symbol, price, market, dbPrice.get().getLastUpdated());
                return price;
            }
        } catch (Exception e) {
            log.error("Error buscando precio en BD para {}: {}", symbol, e.getMessage());
        }
        
        return 0.0;
    }

    /**
     * Obtener historial del valor del portfolio para gráficos
     * Calcula el valor total del portfolio en cada punto temporal usando precios históricos
     */
    public Map<String, Object> getPortfolioHistory(Long userId, int days) {
        try {
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                throw new RuntimeException("Usuario no encontrado");
            }

            List<CryptoPurchase> portfolio = getPortfolio(userId);
            if (portfolio.isEmpty()) {
                return Map.of(
                    "success", true,
                    "dates", List.of(),
                    "values", List.of(),
                    "invested", List.of()
                );
            }

            // Agrupar compras por símbolo
            Map<String, List<CryptoPurchase>> groupedBySymbol = portfolio.stream()
                    .collect(Collectors.groupingBy(CryptoPurchase::getSymbol));

            // Obtener precios históricos de cada símbolo
            Map<String, List<List<Number>>> historicalPrices = new LinkedHashMap<>();
            for (String symbol : groupedBySymbol.keySet()) {
                try {
                    String historyJson = coinGeckoService.getCryptoHistory(symbol, days, "EUR");
                    List<List<Number>> prices = parsePricesFromJson(historyJson);
                    if (prices != null && !prices.isEmpty()) {
                        historicalPrices.put(symbol, prices);
                    }
                } catch (Exception e) {
                    log.error("Error obteniendo historial para {}: {}", symbol, e.getMessage());
                }
            }

            if (historicalPrices.isEmpty()) {
                return Map.of("success", false, "error", "No se pudieron obtener precios históricos");
            }

            // Encontrar timestamps comunes (usar los del primer símbolo como referencia)
            List<List<Number>> referencePrices = historicalPrices.values().iterator().next();
            
            List<String> dates = new ArrayList<>();
            List<Double> portfolioValues = new ArrayList<>();
            List<Double> investedValues = new ArrayList<>();

            for (List<Number> dataPoint : referencePrices) {
                long timestamp = dataPoint.get(0).longValue();
                LocalDateTime pointDate = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()
                );

                double totalValue = 0.0;
                double totalInvested = 0.0;

                for (Map.Entry<String, List<CryptoPurchase>> entry : groupedBySymbol.entrySet()) {
                    String symbol = entry.getKey();
                    List<CryptoPurchase> purchases = entry.getValue();

                    // Calcular cantidad y coste acumulado hasta este punto temporal
                    double quantityAtTime = 0.0;
                    double investedAtTime = 0.0;
                    for (CryptoPurchase purchase : purchases) {
                        if (!purchase.getPurchaseDate().isAfter(pointDate)) {
                            quantityAtTime += purchase.getQuantity();
                            investedAtTime += purchase.getQuantity() * purchase.getPurchasePrice();
                        }
                    }

                    if (quantityAtTime > 0) {
                        // Buscar el precio más cercano para este símbolo en este timestamp
                        double priceAtTime = findClosestPrice(historicalPrices.get(symbol), timestamp);
                        totalValue += quantityAtTime * priceAtTime;
                        totalInvested += investedAtTime;
                    }
                }

                dates.add(String.valueOf(timestamp));
                portfolioValues.add(Math.round(totalValue * 100.0) / 100.0);
                investedValues.add(Math.round(totalInvested * 100.0) / 100.0);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("dates", dates);
            result.put("values", portfolioValues);
            result.put("invested", investedValues);
            return result;

        } catch (Exception e) {
            log.error("Error al obtener historial del portfolio: {}", e.getMessage(), e);
            return Map.of("success", false, "error", "Error: " + e.getMessage());
        }
    }

    /**
     * Parsear precios desde JSON de CoinGecko
     * Formato: {"prices":[[timestamp,price],[timestamp,price],...]}
     */
    @SuppressWarnings("unchecked")
    private List<List<Number>> parsePricesFromJson(String json) {
        try {
            if (json == null || json.contains("\"error\"")) return null;
            
            // Buscar el array "prices"
            int pricesIndex = json.indexOf("\"prices\"");
            if (pricesIndex < 0) return null;
            
            int arrayStart = json.indexOf("[[", pricesIndex);
            int arrayEnd = json.indexOf("]]", arrayStart) + 2;
            if (arrayStart < 0 || arrayEnd < 2) return null;
            
            String pricesStr = json.substring(arrayStart, arrayEnd);
            
            // Parsear manualmente los pares [timestamp, price]
            List<List<Number>> result = new ArrayList<>();
            int i = 0;
            while (i < pricesStr.length()) {
                int pairStart = pricesStr.indexOf("[", i + 1);
                if (pairStart < 0) break;
                int pairEnd = pricesStr.indexOf("]", pairStart);
                if (pairEnd < 0) break;
                
                String pair = pricesStr.substring(pairStart + 1, pairEnd);
                String[] parts = pair.split(",");
                if (parts.length == 2) {
                    try {
                        long ts = Long.parseLong(parts[0].trim());
                        double price = Double.parseDouble(parts[1].trim());
                        result.add(List.of(ts, price));
                    } catch (NumberFormatException ignored) {}
                }
                i = pairEnd + 1;
            }
            
            return result;
        } catch (Exception e) {
            log.error("Error parseando precios: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Encontrar el precio más cercano a un timestamp dado
     */
    private double findClosestPrice(List<List<Number>> prices, long targetTimestamp) {
        if (prices == null || prices.isEmpty()) return 0.0;
        
        double closestPrice = prices.get(0).get(1).doubleValue();
        long minDiff = Long.MAX_VALUE;
        
        for (List<Number> dataPoint : prices) {
            long diff = Math.abs(dataPoint.get(0).longValue() - targetTimestamp);
            if (diff < minDiff) {
                minDiff = diff;
                closestPrice = dataPoint.get(1).doubleValue();
            }
        }
        
        return closestPrice;
    }
}