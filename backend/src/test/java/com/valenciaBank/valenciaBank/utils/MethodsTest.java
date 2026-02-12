package com.valenciaBank.valenciaBank.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Methods - Tests unitarios de utilidades")
class MethodsTest {

    @Test
    @DisplayName("generateAccountNumber genera número de 16 dígitos")
    void generateAccountNumber16Digitos() {
        String accountNumber = Methods.generateAccountNumber();
        assertEquals(16, accountNumber.length());
    }

    @Test
    @DisplayName("generateAccountNumber solo contiene dígitos")
    void generateAccountNumberSoloDigitos() {
        String accountNumber = Methods.generateAccountNumber();
        assertTrue(accountNumber.matches("\\d{16}"), "El número de cuenta debe contener solo dígitos");
    }

    @Test
    @DisplayName("generateAccountNumber genera números diferentes")
    void generateAccountNumberDiferentes() {
        Set<String> numbers = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            numbers.add(Methods.generateAccountNumber());
        }
        // Con 16 dígitos aleatorios, 100 números deberían ser todos diferentes
        assertTrue(numbers.size() > 90, "Los números de cuenta deberían ser mayormente únicos");
    }

    @RepeatedTest(5)
    @DisplayName("generateAccountNumber siempre genera 16 dígitos (repetido)")
    void generateAccountNumberConsistente() {
        String number = Methods.generateAccountNumber();
        assertEquals(16, number.length());
        assertTrue(number.matches("\\d+"));
    }
}
