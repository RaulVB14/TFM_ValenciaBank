package com.valenciaBank.valenciaBank.repository;

import com.valenciaBank.valenciaBank.model.CryptoPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CryptoPriceRepository extends JpaRepository<CryptoPrice, Long> {
    
    /**
     * Busca el precio de una criptomoneda en un mercado específico
     */
    Optional<CryptoPrice> findBySymbolAndMarket(String symbol, String market);
    
    /**
     * Busca todos los precios de una criptomoneda
     */
    Optional<CryptoPrice> findBySymbol(String symbol);
}
