package com.valenciaBank.valenciaBank.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CryptoId - Tests unitarios de la clave compuesta")
class CryptoIdTest {

    @Test
    @DisplayName("Dos CryptoId iguales son equals")
    void equalsConMismosValores() {
        CryptoId id1 = new CryptoId("BTC", LocalDate.of(2026, 1, 1));
        CryptoId id2 = new CryptoId("BTC", LocalDate.of(2026, 1, 1));

        assertEquals(id1, id2);
    }

    @Test
    @DisplayName("Dos CryptoId diferentes no son equals")
    void equalsConDiferentesValores() {
        CryptoId id1 = new CryptoId("BTC", LocalDate.of(2026, 1, 1));
        CryptoId id2 = new CryptoId("ETH", LocalDate.of(2026, 1, 1));

        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("hashCode es consistente con equals")
    void hashCodeConsistente() {
        CryptoId id1 = new CryptoId("BTC", LocalDate.of(2026, 1, 1));
        CryptoId id2 = new CryptoId("BTC", LocalDate.of(2026, 1, 1));

        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    @DisplayName("equals con null retorna false")
    void equalsConNull() {
        CryptoId id = new CryptoId("BTC", LocalDate.of(2026, 1, 1));
        assertNotEquals(null, id);
    }

    @Test
    @DisplayName("equals con distinto tipo retorna false")
    void equalsConDistintoTipo() {
        CryptoId id = new CryptoId("BTC", LocalDate.of(2026, 1, 1));
        assertNotEquals("BTC", id);
    }

    @Test
    @DisplayName("equals reflexivo")
    void equalsReflexivo() {
        CryptoId id = new CryptoId("BTC", LocalDate.of(2026, 1, 1));
        assertEquals(id, id);
    }

    @Test
    @DisplayName("Constructor vacío crea CryptoId válido")
    void constructorVacio() {
        CryptoId id = new CryptoId();
        assertNotNull(id);
    }
}
