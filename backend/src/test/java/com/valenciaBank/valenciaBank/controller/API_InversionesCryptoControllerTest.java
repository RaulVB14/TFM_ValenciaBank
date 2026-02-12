package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.service.CryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("API_InversionesCryptoController - Tests unitarios")
class API_InversionesCryptoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CryptoService cryptoService;

    @InjectMocks
    private API_InversionesCryptoController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /digitalCurrencyDaily retorna datos diarios de crypto")
    void getDigitalCurrencyDaily() throws Exception {
        String mockResponse = "{\"Meta Data\":{\"1. Information\":\"Daily Digital Currency\"}}";
        when(cryptoService.llamarAPI("BTC", "USD")).thenReturn(mockResponse);

        mockMvc.perform(get("/digitalCurrencyDaily")
                        .param("symbol", "BTC")
                        .param("market", "USD"))
                .andExpect(status().isOk())
                .andExpect(content().string(mockResponse));
    }

    @Test
    @DisplayName("GET /equityDaily retorna datos diarios de equity")
    void getEquityDaily() throws Exception {
        String mockResponse = "{\"Time Series (Daily)\":{}}";
        when(cryptoService.llamarEquityAPI("AAPL")).thenReturn(mockResponse);

        mockMvc.perform(get("/equityDaily").param("symbol", "AAPL"))
                .andExpect(status().isOk())
                .andExpect(content().string(mockResponse));
    }

    @Test
    @DisplayName("GET /etfProfile retorna perfil de ETF")
    void getETFProfile() throws Exception {
        String mockResponse = "{\"symbol\":\"SPY\",\"name\":\"SPDR S&P 500\"}";
        when(cryptoService.getETFProfile("SPY")).thenReturn(mockResponse);

        mockMvc.perform(get("/etfProfile").param("symbol", "SPY"))
                .andExpect(status().isOk())
                .andExpect(content().string(mockResponse));
    }

    @Test
    @DisplayName("POST /datos guarda JSON manualmente")
    void recibirJsonExitoso() throws Exception {
        doNothing().when(cryptoService).saveDataFromManualJson(anyString());

        mockMvc.perform(post("/datos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"BTC\",\"price\":50000}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Datos de Bitcoin guardados correctamente"));
    }

    @Test
    @DisplayName("POST /datos error al procesar JSON retorna 400")
    void recibirJsonError() throws Exception {
        doThrow(new RuntimeException("JSON inválido"))
                .when(cryptoService).saveDataFromManualJson(anyString());

        mockMvc.perform(post("/datos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error al procesar el JSON"));
    }
}
