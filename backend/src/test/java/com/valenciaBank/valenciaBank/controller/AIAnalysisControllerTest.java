package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.service.AIAnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AIAnalysisController - Tests unitarios")
class AIAnalysisControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AIAnalysisService aiAnalysisService;

    @InjectMocks
    private AIAnalysisController aiAnalysisController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiAnalysisController).build();
    }

    @Test
    @DisplayName("POST /api/ai/analyze análisis exitoso de mercado")
    void analyzeMarketTrendExitoso() throws Exception {
        when(aiAnalysisService.generateAnalysis(eq("BTC"), eq("crypto"), anyList(), eq("es")))
                .thenReturn("El mercado muestra tendencia alcista...");

        Map<String, Object> request = Map.of(
                "symbol", "BTC",
                "type", "crypto",
                "language", "es",
                "prices", List.of(50000.0, 51000.0, 52000.0)
        );

        mockMvc.perform(post("/api/ai/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.symbol").value("BTC"))
                .andExpect(jsonPath("$.analysis").value("El mercado muestra tendencia alcista..."));
    }

    @Test
    @DisplayName("POST /api/ai/analyze sin precios retorna error 400")
    void analyzeMarketTrendSinPrecios() throws Exception {
        Map<String, Object> request = Map.of(
                "symbol", "ETH",
                "type", "crypto",
                "prices", List.of()
        );

        mockMvc.perform(post("/api/ai/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/ai/analyze excepción en servicio retorna error 500")
    void analyzeMarketTrendExcepcion() throws Exception {
        when(aiAnalysisService.generateAnalysis(anyString(), anyString(), anyList(), anyString()))
                .thenThrow(new RuntimeException("API no disponible"));

        Map<String, Object> request = Map.of(
                "symbol", "BTC",
                "type", "crypto",
                "prices", List.of(50000.0, 51000.0)
        );

        mockMvc.perform(post("/api/ai/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Error al generar análisis: API no disponible"));
    }

    @Test
    @DisplayName("POST /api/ai/analyze usa valores por defecto")
    void analyzeMarketTrendValoresPorDefecto() throws Exception {
        when(aiAnalysisService.generateAnalysis(eq("BTC"), eq("crypto"), anyList(), eq("es")))
                .thenReturn("Análisis por defecto");

        Map<String, Object> request = Map.of(
                "prices", List.of(100.0, 200.0)
        );

        mockMvc.perform(post("/api/ai/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
