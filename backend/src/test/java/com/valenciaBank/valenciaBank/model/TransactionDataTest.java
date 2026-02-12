package com.valenciaBank.valenciaBank.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionData - Tests unitarios del modelo")
class TransactionDataTest {

    @Test
    @DisplayName("Constructor con parámetros asigna correctamente")
    void constructorConParametros() {
        Transaction transaction = new Transaction();
        transaction.setAmount(100.0);
        transaction.setOriginAccount("1111");
        transaction.setDestinationAccount("2222");

        TransactionData data = new TransactionData(transaction, "12345678A");

        assertNotNull(data.getTransaction());
        assertEquals(100.0, data.getTransaction().getAmount());
        assertEquals("12345678A", data.getUser());
    }

    @Test
    @DisplayName("Constructor vacío y setters")
    void constructorVacioYSetters() {
        TransactionData data = new TransactionData();
        Transaction transaction = new Transaction();
        transaction.setAmount(250.0);

        data.setTransaction(transaction);
        data.setUser("99999999Z");

        assertEquals(250.0, data.getTransaction().getAmount());
        assertEquals("99999999Z", data.getUser());
    }
}
