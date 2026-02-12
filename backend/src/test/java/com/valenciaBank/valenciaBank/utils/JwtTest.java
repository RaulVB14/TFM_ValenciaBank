package com.valenciaBank.valenciaBank.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Jwt - Tests unitarios de generación y validación JWT")
class JwtTest {

    @BeforeEach
    void setUp() {
        // Inyectar clave de prueba manualmente (simula @Value)
        Jwt jwt = new Jwt();
        jwt.setSecretKey("TestSecretKeyForJWT2026");
    }

    @Test
    @DisplayName("generateToken genera un token no nulo")
    void generateTokenNoNulo() {
        String token = Jwt.generateToken("12345678A");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("generateToken genera token con formato JWT válido (3 partes)")
    void generateTokenFormatoValido() {
        String token = Jwt.generateToken("12345678A");
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "Un JWT debe tener 3 partes separadas por puntos");
    }

    @Test
    @DisplayName("validateToken devuelve el DNI correcto")
    void validateTokenDevuelveDni() {
        String dni = "12345678A";
        String token = Jwt.generateToken(dni);
        String result = Jwt.validateToken(token);
        assertEquals(dni, result);
    }

    @Test
    @DisplayName("validateToken con token inválido devuelve null")
    void validateTokenInvalidoDevuelveNull() {
        String result = Jwt.validateToken("token.invalido.123");
        assertNull(result);
    }

    @Test
    @DisplayName("validateToken con token vacío devuelve null")
    void validateTokenVacioDevuelveNull() {
        String result = Jwt.validateToken("");
        assertNull(result);
    }

    @Test
    @DisplayName("Tokens diferentes para DNIs diferentes")
    void tokensDiferentesParaDnisDiferentes() {
        String token1 = Jwt.generateToken("11111111A");
        String token2 = Jwt.generateToken("22222222B");
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Token generado puede ser validado inmediatamente")
    void tokenValidoInmediatamente() {
        String dni = "99999999Z";
        String token = Jwt.generateToken(dni);
        String validatedDni = Jwt.validateToken(token);
        assertEquals(dni, validatedDni);
    }

    @Test
    @DisplayName("validateToken con token manipulado devuelve null")
    void validateTokenManipulado() {
        String token = Jwt.generateToken("12345678A");
        // Manipular el payload
        String manipulated = token.substring(0, token.length() - 5) + "XXXXX";
        String result = Jwt.validateToken(manipulated);
        assertNull(result);
    }
}
