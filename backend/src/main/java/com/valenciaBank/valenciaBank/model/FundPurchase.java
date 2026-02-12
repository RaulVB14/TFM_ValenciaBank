package com.valenciaBank.valenciaBank.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
public class FundPurchase implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private String symbol; // SPY, VOO, XLK, etc.

    @Column(nullable = false)
    private String name; // Nombre descriptivo: "S&P 500", "Vanguard S&P 500", etc.

    @Column(nullable = false)
    private String type; // "ETF" o "INDEX"

    @Column(nullable = false)
    private Double quantity; // Cantidad de participaciones

    @Column(nullable = false)
    private Double purchasePrice; // Precio pagado por unidad en el momento de compra

    @Column(nullable = false)
    private LocalDateTime purchaseDate;

    @Column(nullable = false)
    private String currency; // EUR, USD, etc.

    public FundPurchase() {}

    public FundPurchase(User user, String symbol, String name, String type, Double quantity, Double purchasePrice, String currency) {
        this.user = user;
        this.symbol = symbol;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.currency = currency;
        this.purchaseDate = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public Double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(Double purchasePrice) { this.purchasePrice = purchasePrice; }

    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Double getTotalCost() {
        return quantity * purchasePrice;
    }

    @Override
    public String toString() {
        return "FundPurchase{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", quantity=" + quantity +
                ", purchasePrice=" + purchasePrice +
                ", purchaseDate=" + purchaseDate +
                ", currency='" + currency + '\'' +
                '}';
    }
}
