package com.valenciaBank.valenciaBank.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Transaction - Tests unitarios del modelo")
class TransactionTest {

    @Test
    @DisplayName("Constructor por defecto asigna fecha actual")
    void constructorAsignaFecha() {
        Transaction transaction = new Transaction();
        assertNotNull(transaction.getDate());
    }

    @Test
    @DisplayName("Setters y getters funcionan correctamente")
    void settersYGetters() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setOriginAccount("1111111111111111");
        transaction.setDestinationAccount("2222222222222222");
        transaction.setAmount(500.0);

        assertEquals(1L, transaction.getId());
        assertEquals("1111111111111111", transaction.getOriginAccount());
        assertEquals("2222222222222222", transaction.getDestinationAccount());
        assertEquals(500.0, transaction.getAmount());
    }

    @Test
    @DisplayName("Se puede asignar un usuario a la transacción")
    void asignarUsuario() {
        Transaction transaction = new Transaction();
        User user = new User();
        user.setId(1L);

        transaction.setUser(user);
        assertNotNull(transaction.getUser());
        assertEquals(1L, transaction.getUser().getId());
    }

    @Test
    @DisplayName("Monto puede ser decimal")
    void montoDecimal() {
        Transaction transaction = new Transaction();
        transaction.setAmount(123.45);
        assertEquals(123.45, transaction.getAmount(), 0.001);
    }
}
