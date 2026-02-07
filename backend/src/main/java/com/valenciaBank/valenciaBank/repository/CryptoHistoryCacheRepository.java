package com.valenciaBank.valenciaBank.repository;

import com.valenciaBank.valenciaBank.model.CryptoHistoryCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CryptoHistoryCacheRepository extends JpaRepository<CryptoHistoryCache, Long> {

    /**
     * Buscar caché de histórico por símbolo, días y moneda
     * Usa findFirst para evitar error si hay duplicados residuales
     */
    Optional<CryptoHistoryCache> findFirstBySymbolAndDaysAndCurrency(String symbol, Integer days, String currency);

    /**
     * Eliminar caché por símbolo, días y moneda
     */
    void deleteBySymbolAndDaysAndCurrency(String symbol, Integer days, String currency);
}
