package com.valenciaBank.valenciaBank.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para almacenar datos históricos de criptomonedas en caché
 * Evita hacer llamadas repetidas a CoinGecko y respeta los rate limits
 */
@Entity
@Table(name = "CRYPTO_HISTORY_CACHE",
    uniqueConstraints = @UniqueConstraint(name = "uk_symbol_days_currency", columnNames = {"symbol", "days", "currency"}),
    indexes = {
        @Index(name = "idx_symbol_days_currency", columnList = "symbol,days,currency"),
        @Index(name = "idx_last_fetched", columnList = "last_fetched")
    }
)
public class CryptoHistoryCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol; // BTC, ETH, etc.

    @Column(nullable = false)
    private Integer days; // 1, 7, 30, 90, 365

    @Column(nullable = false)
    private String currency; // EUR, USD

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String historyData; // JSON con los datos históricos

    @Column(nullable = false)
    private LocalDateTime lastFetched; // Cuándo se obtuvieron los datos

    @Column(nullable = false)
    private LocalDateTime expiresAt; // Cuándo expira el caché (24 horas)

    public CryptoHistoryCache() {
    }

    public CryptoHistoryCache(String symbol, Integer days, String currency, String historyData) {
        this.symbol = symbol;
        this.days = days;
        this.currency = currency;
        this.historyData = historyData;
        this.lastFetched = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(24);
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getHistoryData() {
        return historyData;
    }

    public void setHistoryData(String historyData) {
        this.historyData = historyData;
    }

    public LocalDateTime getLastFetched() {
        return lastFetched;
    }

    public void setLastFetched(LocalDateTime lastFetched) {
        this.lastFetched = lastFetched;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Verificar si el caché sigue siendo válido
     */
    public boolean isValid() {
        return LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Refrescar la expiración del caché (24 horas desde ahora)
     */
    public void refreshExpiration() {
        this.lastFetched = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(24);
    }
}
