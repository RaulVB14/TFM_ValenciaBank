package com.valenciaBank.valenciaBank.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Account - Tests unitarios del modelo")
class AccountTest {

    @Test
    @DisplayName("Constructor por defecto asigna fecha de creación")
    void constructorAsignaFecha() {
        Account account = new Account();
        assertNotNull(account.getCreationDate());
    }

    @Test
    @DisplayName("Setters y getters funcionan correctamente")
    void settersYGetters() {
        Account account = new Account();
        account.setId(1L);
        account.setBalance(5000.0);
        account.setNumber("1234567890123456");

        assertEquals(1L, account.getId());
        assertEquals(5000.0, account.getBalance());
        assertEquals("1234567890123456", account.getNumber());
    }

    @Test
    @DisplayName("Se puede asignar un usuario a la cuenta")
    void asignarUsuario() {
        Account account = new Account();
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        account.setUser(user);

        assertNotNull(account.getUser());
        assertEquals("testuser", account.getUser().getUsername());
    }

    @Test
    @DisplayName("Balance se inicializa a 0 por defecto")
    void balanceInicialCero() {
        Account account = new Account();
        assertEquals(0.0, account.getBalance());
    }

    @Test
    @DisplayName("Se puede modificar la fecha de creación")
    void modificarFechaCreacion() {
        Account account = new Account();
        Date customDate = new Date(0);
        account.setCreationDate(customDate);
        assertEquals(customDate, account.getCreationDate());
    }
}
