package com.valenciaBank.valenciaBank.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CryptoPrice - Tests unitarios del modelo")
class CryptoPriceTest {

    @Test
    @DisplayName("Constructor con parámetros asigna valores y fecha")
    void constructorConParametros() {
        CryptoPrice price = new CryptoPrice("BTC", "EUR", 95000.0);

        assertEquals("BTC", price.getSymbol());
        assertEquals("EUR", price.getMarket());
        assertEquals(95000.0, price.getPrice());
        assertNotNull(price.getLastUpdated());
    }

    @Test
    @DisplayName("Constructor vacío permite setters")
    void constructorVacio() {
        CryptoPrice price = new CryptoPrice();
        price.setId(1L);
        price.setSymbol("ETH");
        price.setMarket("USD");
        price.setPrice(3500.0);
        price.setLastUpdated(LocalDateTime.now());

        assertEquals(1L, price.getId());
        assertEquals("ETH", price.getSymbol());
        assertEquals("USD", price.getMarket());
        assertEquals(3500.0, price.getPrice());
    }
}
