package com.valenciaBank.valenciaBank.repository;

import com.valenciaBank.valenciaBank.model.CryptoPurchase;
import com.valenciaBank.valenciaBank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CryptoPurchaseRepository extends JpaRepository<CryptoPurchase, Long> {
    // Obtener todas las compras de un usuario
    List<CryptoPurchase> findByUser(User user);

    // Obtener compras de una cripto específica de un usuario
    List<CryptoPurchase> findByUserAndSymbol(User user, String symbol);

    // Obtener la cantidad total de una cripto que posee un usuario
    List<CryptoPurchase> findByUserIdAndSymbol(Long userId, String symbol);
}
