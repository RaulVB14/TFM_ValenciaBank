package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.CryptoPurchase;
import com.valenciaBank.valenciaBank.model.User;
import com.valenciaBank.valenciaBank.repository.CryptoPurchaseRepository;
import com.valenciaBank.valenciaBank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PortfolioService {

    @Autowired
    private CryptoPurchaseRepository cryptoPurchaseRepository;

    @Autowired
    private UserRepository userRepository;

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
}
