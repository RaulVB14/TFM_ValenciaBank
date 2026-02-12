package com.valenciaBank.valenciaBank.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinnhubService - Tests unitarios")
class FinnhubServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FinnhubService finnhubService;

    @Test
    @DisplayName("getEtfData retorna error si API key no configurada")
    void getEtfDataSinApiKey() {
        // finnhubApiKey es null por defecto
        String result = finnhubService.getEtfData("SPY");
        assertTrue(result.contains("API key de Finnhub no configurada"));
    }

    @Test
    @DisplayName("getEtfCandles retorna error si API key no configurada")
    void getEtfCandlesSinApiKey() {
        String result = finnhubService.getEtfCandles("SPY", "D", 30);
        assertTrue(result.contains("API key de Finnhub no configurada"));
    }

    @Test
    @DisplayName("searchEtf retorna error si API key no configurada")
    void searchEtfSinApiKey() {
        String result = finnhubService.searchEtf("SPY");
        assertTrue(result.contains("API key de Finnhub no configurada"));
    }

    @Test
    @DisplayName("getEtfCandles rechaza símbolo no soportado")
    void getEtfCandlesSimboloNoSoportado() {
        // Necesitamos API key para llegar al check de símbolo, usamos reflection
        try {
            var field = FinnhubService.class.getDeclaredField("finnhubApiKey");
            field.setAccessible(true);
            field.set(finnhubService, "test-key");
        } catch (Exception e) {
            fail("No se pudo configurar API key de test");
        }

        String result = finnhubService.getEtfCandles("INVALID_SYMBOL", "D", 30);
        assertTrue(result.contains("no está disponible"));
    }
}
