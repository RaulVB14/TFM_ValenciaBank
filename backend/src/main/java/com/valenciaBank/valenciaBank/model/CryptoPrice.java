package com.valenciaBank.valenciaBank.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CRYPTO_PRICE", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"symbol", "market"})
})
public class CryptoPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol; // BTC, ETH, ADA, etc.

    @Column(nullable = false)
    private String market; // EUR, USD, etc.

    @Column(nullable = false)
    private Double price;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    public CryptoPrice() {
    }

    public CryptoPrice(String symbol, String market, Double price) {
        this.symbol = symbol;
        this.market = market;
        this.price = price;
        this.lastUpdated = LocalDateTime.now();
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

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
