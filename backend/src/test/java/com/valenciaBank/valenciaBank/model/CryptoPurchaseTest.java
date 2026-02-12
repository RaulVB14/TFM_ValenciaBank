package com.valenciaBank.valenciaBank.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CryptoPurchase - Tests unitarios del modelo")
class CryptoPurchaseTest {

    @Test
    @DisplayName("Constructor con parámetros asigna valores correctamente")
    void constructorConParametros() {
        User user = new User();
        user.setId(1L);

        CryptoPurchase purchase = new CryptoPurchase(user, "BTC", 0.5, 95000.0, "EUR");

        assertEquals(user, purchase.getUser());
        assertEquals("BTC", purchase.getSymbol());
        assertEquals(0.5, purchase.getQuantity());
        assertEquals(95000.0, purchase.getPurchasePrice());
        assertEquals("EUR", purchase.getCurrency());
        assertNotNull(purchase.getPurchaseDate());
    }

    @Test
    @DisplayName("getTotalCost calcula cantidad * precio correctamente")
    void getTotalCost() {
        CryptoPurchase purchase = new CryptoPurchase(new User(), "BTC", 0.5, 90000.0, "EUR");
        assertEquals(45000.0, purchase.getTotalCost(), 0.01);
    }

    @Test
    @DisplayName("getTotalCost con cantidad pequeña")
    void getTotalCostCantidadPequena() {
        CryptoPurchase purchase = new CryptoPurchase(new User(), "BTC", 0.001, 95000.0, "EUR");
        assertEquals(95.0, purchase.getTotalCost(), 0.01);
    }

    @Test
    @DisplayName("Setters modifican valores correctamente")
    void settersModifican() {
        CryptoPurchase purchase = new CryptoPurchase();
        purchase.setId(10L);
        purchase.setSymbol("ETH");
        purchase.setQuantity(2.5);
        purchase.setPurchasePrice(3500.0);
        purchase.setCurrency("USD");
        purchase.setPurchaseDate(LocalDateTime.of(2026, 1, 15, 10, 30));

        assertEquals(10L, purchase.getId());
        assertEquals("ETH", purchase.getSymbol());
        assertEquals(2.5, purchase.getQuantity());
        assertEquals(3500.0, purchase.getPurchasePrice());
        assertEquals("USD", purchase.getCurrency());
    }

    @Test
    @DisplayName("toString contiene información de la compra")
    void toStringContieneInfo() {
        CryptoPurchase purchase = new CryptoPurchase(new User(), "BTC", 0.5, 95000.0, "EUR");
        String str = purchase.toString();
        assertTrue(str.contains("BTC"));
        assertTrue(str.contains("0.5"));
        assertTrue(str.contains("95000.0"));
    }
}
