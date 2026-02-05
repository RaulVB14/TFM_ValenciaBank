package com.valenciaBank.valenciaBank.repository;

import com.valenciaBank.valenciaBank.model.CryptoHistoryCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CryptoHistoryCacheRepository extends JpaRepository<CryptoHistoryCache, Long> {

    /**
     * Buscar caché de histórico por símbolo, días y moneda
     */
    Optional<CryptoHistoryCache> findBySymbolAndDaysAndCurrency(String symbol, Integer days, String currency);

    /**
     * Eliminar caché expirado (si es necesario)
     */
    void deleteBySymbolAndDaysAndCurrency(String symbol, Integer days, String currency);
}
