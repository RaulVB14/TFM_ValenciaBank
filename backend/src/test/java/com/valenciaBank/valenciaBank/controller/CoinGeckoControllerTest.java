package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.service.CoinGeckoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CoinGeckoController - Tests unitarios")
class CoinGeckoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CoinGeckoService coinGeckoService;

    @InjectMocks
    private CoinGeckoController coinGeckoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(coinGeckoController).build();
    }

    @Test
    @DisplayName("GET /api/coingecko/price/{symbol}/{currency} retorna precio")
    void getCryptoPriceExitoso() throws Exception {
        String mockResponse = "{\"bitcoin\":{\"usd\":50000.0}}";
        when(coinGeckoService.getCryptoData("BTC", "USD")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/coingecko/price/BTC/USD"))
                .andExpect(status().isOk())
                .andExpect(content().json(mockResponse));
    }

    @Test
    @DisplayName("GET /api/coingecko/price/{symbol}/{currency} error retorna 400")
    void getCryptoPriceError() throws Exception {
        when(coinGeckoService.getCryptoData("INVALIDA", "USD"))
                .thenThrow(new RuntimeException("Símbolo no soportado"));

        mockMvc.perform(get("/api/coingecko/price/INVALIDA/USD"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/coingecko/history/{symbol}/{days}/{currency} retorna historial")
    void getCryptoHistoryExitoso() throws Exception {
        String mockResponse = "{\"prices\":[[1625097600000,34000.0]]}";
        when(coinGeckoService.getCryptoHistory("BTC", 7, "USD")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/coingecko/history/BTC/7/USD"))
                .andExpect(status().isOk())
                .andExpect(content().json(mockResponse));
    }

    @Test
    @DisplayName("GET /api/coingecko/history/{symbol}/{days}/{currency} error retorna 400")
    void getCryptoHistoryError() throws Exception {
        when(coinGeckoService.getCryptoHistory(eq("FAIL"), anyInt(), eq("USD")))
                .thenThrow(new RuntimeException("Error de API"));

        mockMvc.perform(get("/api/coingecko/history/FAIL/30/USD"))
                .andExpect(status().isBadRequest());
    }
}
