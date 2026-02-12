package com.valenciaBank.valenciaBank.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FundPurchase - Tests unitarios del modelo")
class FundPurchaseTest {

    @Test
    @DisplayName("Constructor con parámetros asigna todos los valores")
    void constructorConParametros() {
        User user = new User();
        user.setId(1L);

        FundPurchase fund = new FundPurchase(user, "SPY", "SPDR S&P 500", "ETF", 10.0, 495.0, "USD");

        assertEquals("SPY", fund.getSymbol());
        assertEquals("SPDR S&P 500", fund.getName());
        assertEquals("ETF", fund.getType());
        assertEquals(10.0, fund.getQuantity());
        assertEquals(495.0, fund.getPurchasePrice());
        assertEquals("USD", fund.getCurrency());
        assertNotNull(fund.getPurchaseDate());
    }

    @Test
    @DisplayName("getTotalCost calcula correctamente")
    void getTotalCost() {
        FundPurchase fund = new FundPurchase(new User(), "VOO", "Vanguard S&P 500", "ETF", 5.0, 480.0, "USD");
        assertEquals(2400.0, fund.getTotalCost(), 0.01);
    }

    @Test
    @DisplayName("Setters y getters funcionan correctamente")
    void settersYGetters() {
        FundPurchase fund = new FundPurchase();
        fund.setId(5L);
        fund.setSymbol("QQQ");
        fund.setName("Invesco QQQ");
        fund.setType("ETF");
        fund.setQuantity(3.0);
        fund.setPurchasePrice(400.0);
        fund.setCurrency("USD");
        fund.setPurchaseDate(LocalDateTime.of(2026, 2, 10, 14, 0));

        assertEquals(5L, fund.getId());
        assertEquals("QQQ", fund.getSymbol());
        assertEquals("Invesco QQQ", fund.getName());
        assertEquals("ETF", fund.getType());
        assertEquals(3.0, fund.getQuantity());
    }

    @Test
    @DisplayName("toString contiene información del fondo")
    void toStringContieneInfo() {
        FundPurchase fund = new FundPurchase(new User(), "SPY", "SPDR S&P 500", "ETF", 10.0, 495.0, "USD");
        String str = fund.toString();
        assertTrue(str.contains("SPY"));
        assertTrue(str.contains("SPDR S&P 500"));
        assertTrue(str.contains("ETF"));
    }
}
