package com.valenciaBank.valenciaBank.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User - Tests unitarios del modelo")
class UserTest {

    @Test
    @DisplayName("Constructor vacío crea usuario sin valores")
    void constructorVacio() {
        User user = new User();
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getDni());
    }

    @Test
    @DisplayName("Setters y getters funcionan correctamente")
    void settersYGetters() {
        User user = new User();
        user.setId(1L);
        user.setUsername("raul_vb");
        user.setPassword("encrypted123");
        user.setDni("12345678A");
        user.setNombre("Raúl");
        user.setApellidos("VB");
        user.setEmail("raul@test.com");
        user.setTelefono("600123456");
        user.setDireccion("Valencia, España");

        assertEquals(1L, user.getId());
        assertEquals("raul_vb", user.getUsername());
        assertEquals("encrypted123", user.getPassword());
        assertEquals("12345678A", user.getDni());
        assertEquals("Raúl", user.getNombre());
        assertEquals("VB", user.getApellidos());
        assertEquals("raul@test.com", user.getEmail());
        assertEquals("600123456", user.getTelefono());
        assertEquals("Valencia, España", user.getDireccion());
    }

    @Test
    @DisplayName("Se puede asignar una cuenta al usuario")
    void asignarCuenta() {
        User user = new User();
        Account account = new Account();
        account.setNumber("1234567890123456");
        account.setBalance(1000.0);

        user.setAccount(account);

        assertNotNull(user.getAccount());
        assertEquals("1234567890123456", user.getAccount().getNumber());
    }

    @Test
    @DisplayName("toString contiene información del usuario")
    void toStringContieneInfo() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setDni("11111111A");

        String str = user.toString();
        assertTrue(str.contains("testuser"));
        assertTrue(str.contains("11111111A"));
    }
}
