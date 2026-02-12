package com.valenciaBank.valenciaBank.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Crypto - Tests unitarios del modelo")
class CryptoTest {

    @Test
    @DisplayName("Constructor con parámetros asigna todos los valores")
    void constructorConParametros() {
        LocalDate date = LocalDate.of(2026, 1, 15);
        Crypto crypto = new Crypto("BTC", date, 95000.0, 96000.0, 94000.0, 94500.0, 1500000.0);

        assertEquals("BTC", crypto.getName());
        assertEquals(date, crypto.getDate());
        assertEquals(95000.0, crypto.getClose());
        assertEquals(96000.0, crypto.getHigh());
        assertEquals(94000.0, crypto.getLow());
        assertEquals(94500.0, crypto.getOpen());
        assertEquals(1500000.0, crypto.getVolume());
    }

    @Test
    @DisplayName("Constructor vacío permite setters")
    void constructorVacio() {
        Crypto crypto = new Crypto();
        crypto.setName("ETH");
        crypto.setDate(LocalDate.of(2026, 2, 1));
        crypto.setClose(3500.0);
        crypto.setHigh(3600.0);
        crypto.setLow(3400.0);
        crypto.setOpen(3450.0);
        crypto.setVolume(500000.0);

        assertEquals("ETH", crypto.getName());
        assertEquals(3500.0, crypto.getClose());
    }

    @Test
    @DisplayName("toString contiene valores del crypto")
    void toStringContieneInfo() {
        Crypto crypto = new Crypto("BTC", LocalDate.now(), 95000.0, 96000.0, 94000.0, 94500.0, 1500000.0);
        String str = crypto.toString();
        assertTrue(str.contains("95000.0"));
        assertTrue(str.contains("Crypto{"));
    }
}
