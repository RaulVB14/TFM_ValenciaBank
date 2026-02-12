package com.valenciaBank.valenciaBank.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CryptoHistoryCache - Tests unitarios del modelo")
class CryptoHistoryCacheTest {

    @Test
    @DisplayName("Constructor con parámetros configura expiración a 24h")
    void constructorConParametros() {
        CryptoHistoryCache cache = new CryptoHistoryCache("BTC", 30, "EUR", "{\"prices\":[]}");

        assertEquals("BTC", cache.getSymbol());
        assertEquals(30, cache.getDays());
        assertEquals("EUR", cache.getCurrency());
        assertEquals("{\"prices\":[]}", cache.getHistoryData());
        assertNotNull(cache.getLastFetched());
        assertNotNull(cache.getExpiresAt());
        assertTrue(cache.getExpiresAt().isAfter(cache.getLastFetched()));
    }

    @Test
    @DisplayName("isValid retorna true cuando no ha expirado")
    void isValidNoExpirado() {
        CryptoHistoryCache cache = new CryptoHistoryCache("BTC", 30, "EUR", "{}");
        assertTrue(cache.isValid());
    }

    @Test
    @DisplayName("isValid retorna false cuando ha expirado")
    void isValidExpirado() {
        CryptoHistoryCache cache = new CryptoHistoryCache("BTC", 30, "EUR", "{}");
        cache.setExpiresAt(LocalDateTime.now().minusHours(1));
        assertFalse(cache.isValid());
    }

    @Test
    @DisplayName("refreshExpiration renueva las fechas")
    void refreshExpiration() {
        CryptoHistoryCache cache = new CryptoHistoryCache("BTC", 30, "EUR", "{}");
        // Simular expiración
        cache.setExpiresAt(LocalDateTime.now().minusHours(1));
        assertFalse(cache.isValid());

        // Refrescar
        cache.refreshExpiration();
        assertTrue(cache.isValid());
        assertTrue(cache.getExpiresAt().isAfter(LocalDateTime.now().plusHours(23)));
    }

    @Test
    @DisplayName("Setters y getters funcionan correctamente")
    void settersYGetters() {
        CryptoHistoryCache cache = new CryptoHistoryCache();
        cache.setId(1L);
        cache.setSymbol("ETH");
        cache.setDays(7);
        cache.setCurrency("USD");
        cache.setHistoryData("{\"data\":\"test\"}");

        assertEquals(1L, cache.getId());
        assertEquals("ETH", cache.getSymbol());
        assertEquals(7, cache.getDays());
        assertEquals("USD", cache.getCurrency());
        assertEquals("{\"data\":\"test\"}", cache.getHistoryData());
    }
}
