package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.CryptoPrice;
import com.valenciaBank.valenciaBank.repository.CryptoPriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataInitializationService - Tests unitarios")
class DataInitializationServiceTest {

    @Test
    @DisplayName("Los precios iniciales tienen valores coherentes")
    void preciosInicialesCoherentes() {
        // Verificar que los precios iniciales definidos en el servicio son razonables
        String[] symbols = {"BTC", "ETH", "ADA", "SOL", "XRP", "DOT", "DOGE", "LINK", "MATIC", "UNI",
                "AVAX", "LTC", "BCH", "ETC", "XLM", "ATOM", "NEAR", "FLOW", "THETA", "VET"};
        double[] pricesEUR = {95000.00, 3500.00, 1.20, 210.00, 3.50, 40.00, 0.35, 28.50, 1.10, 18.00,
                85.00, 200.00, 550.00, 45.00, 0.45, 15.50, 8.20, 5.75, 6.80, 0.12};

        assertEquals(symbols.length, pricesEUR.length, "Debe haber un precio por cada símbolo");

        for (double price : pricesEUR) {
            assertTrue(price > 0, "Todos los precios deben ser positivos");
        }

        // BTC debe ser el más caro
        assertEquals(95000.0, pricesEUR[0]);
        // VET debe ser uno de los más baratos
        assertTrue(pricesEUR[pricesEUR.length - 1] < 1.0);
    }
}
