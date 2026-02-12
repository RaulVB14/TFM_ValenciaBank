package com.valenciaBank.valenciaBank.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AIAnalysisService - Tests unitarios")
class AIAnalysisServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AIAnalysisService aiAnalysisService;

    @Test
    @DisplayName("generateAnalysis retorna mensaje si API key no configurada")
    void generateAnalysisSinApiKey() {
        // groqApiKey es null por defecto en el mock
        String result = aiAnalysisService.generateAnalysis("BTC", "crypto", List.of(95000.0, 96000.0), "es");
        assertTrue(result.contains("API key de Groq no configurada"));
    }

    @Test
    @DisplayName("generateAnalysis funciona con lista de precios vacía cuando no hay API key")
    void generateAnalysisSinPreciosNiKey() {
        String result = aiAnalysisService.generateAnalysis("BTC", "crypto", List.of(), "es");
        assertTrue(result.contains("API key"));
    }
}
