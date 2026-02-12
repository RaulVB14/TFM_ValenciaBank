package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.service.YahooFinanceService;
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
@DisplayName("FinnhubController - Tests unitarios")
class FinnhubControllerTest {

    private MockMvc mockMvc;

    @Mock
    private YahooFinanceService yahooFinanceService;

    @InjectMocks
    private FinnhubController finnhubController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(finnhubController).build();
    }

    @Test
    @DisplayName("GET /api/finnhub/etf/{symbol} retorna datos del ETF")
    void getEtfDataExitoso() throws Exception {
        String mockResponse = "{\"symbol\":\"SPY\",\"price\":495.0}";
        when(yahooFinanceService.getQuote("SPY")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/finnhub/etf/SPY"))
                .andExpect(status().isOk())
                .andExpect(content().json(mockResponse));
    }

    @Test
    @DisplayName("GET /api/finnhub/etf/{symbol} error retorna 400")
    void getEtfDataError() throws Exception {
        when(yahooFinanceService.getQuote("INVALID"))
                .thenThrow(new RuntimeException("Símbolo no encontrado"));

        mockMvc.perform(get("/api/finnhub/etf/INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/finnhub/candles/{symbol}/{resolution}/{days} retorna velas")
    void getEtfCandlesExitoso() throws Exception {
        String mockResponse = "{\"prices\":[{\"date\":1625097600,\"close\":495.0}]}";
        when(yahooFinanceService.getCandles("VOO", 30)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/finnhub/candles/VOO/D/30"))
                .andExpect(status().isOk())
                .andExpect(content().json(mockResponse));
    }

    @Test
    @DisplayName("GET /api/finnhub/candles error retorna 400")
    void getEtfCandlesError() throws Exception {
        when(yahooFinanceService.getCandles(eq("FAIL"), anyInt()))
                .thenThrow(new RuntimeException("Error al obtener datos"));

        mockMvc.perform(get("/api/finnhub/candles/FAIL/D/7"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/finnhub/search/{query} retorna resultados de búsqueda")
    void searchEtfExitoso() throws Exception {
        String mockResponse = "{\"quotes\":[{\"symbol\":\"SPY\"}]}";
        when(yahooFinanceService.search("SPY")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/finnhub/search/SPY"))
                .andExpect(status().isOk())
                .andExpect(content().json(mockResponse));
    }
}
