package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.model.CryptoPrice;
import com.valenciaBank.valenciaBank.repository.CryptoPriceRepository;
import com.valenciaBank.valenciaBank.service.CryptoService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CryptoPriceController - Tests unitarios")
class CryptoPriceControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CryptoPriceRepository cryptoPriceRepository;

    @Mock
    private CryptoService cryptoService;

    @InjectMocks
    private CryptoPriceController cryptoPriceController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cryptoPriceController).build();
    }

    @Test
    @DisplayName("GET /api/crypto-prices retorna todos los precios")
    void getAllPrices() throws Exception {
        CryptoPrice btc = new CryptoPrice("BTC", "USD", 50000.0);
        CryptoPrice eth = new CryptoPrice("ETH", "USD", 3000.0);
        when(cryptoPriceRepository.findAll()).thenReturn(List.of(btc, eth));

        mockMvc.perform(get("/api/crypto-prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/crypto-prices/{symbol}/{market} retorna precio específico")
    void getPriceExitoso() throws Exception {
        CryptoPrice btc = new CryptoPrice("BTC", "USD", 50000.0);
        when(cryptoPriceRepository.findBySymbolAndMarket("BTC", "USD"))
                .thenReturn(Optional.of(btc));

        mockMvc.perform(get("/api/crypto-prices/BTC/USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.price").value(50000.0));
    }

    @Test
    @DisplayName("GET /api/crypto-prices/{symbol}/{market} no encontrado retorna 404")
    void getPriceNoEncontrado() throws Exception {
        when(cryptoPriceRepository.findBySymbolAndMarket("XYZ", "USD"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/crypto-prices/XYZ/USD"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/crypto-prices actualiza precio existente")
    void updatePriceExitoso() throws Exception {
        CryptoPrice existing = new CryptoPrice("BTC", "USD", 50000.0);
        when(cryptoPriceRepository.findBySymbolAndMarket("BTC", "USD"))
                .thenReturn(Optional.of(existing));
        when(cryptoPriceRepository.save(any(CryptoPrice.class))).thenReturn(existing);

        Map<String, Object> request = Map.of(
                "symbol", "BTC", "market", "USD", "price", 55000.0
        );

        mockMvc.perform(post("/api/crypto-prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/crypto-prices/{id} elimina precio")
    void deletePriceExitoso() throws Exception {
        doNothing().when(cryptoPriceRepository).deleteById(1L);

        mockMvc.perform(delete("/api/crypto-prices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/crypto-prices/update-from-api actualiza desde API")
    void updateFromAPIExitoso() throws Exception {
        String apiResponse = "{\"Realtime Currency Exchange Rate\":{\"5. Exchange Rate\":\"51000.00\"}}";
        when(cryptoService.llamarAPIExterna("BTC", "USD")).thenReturn(apiResponse);
        when(cryptoPriceRepository.findBySymbolAndMarket("BTC", "USD"))
                .thenReturn(Optional.empty());
        CryptoPrice saved = new CryptoPrice("BTC", "USD", 51000.0);
        when(cryptoPriceRepository.save(any(CryptoPrice.class))).thenReturn(saved);

        Map<String, String> request = Map.of("symbol", "BTC", "market", "USD");

        mockMvc.perform(post("/api/crypto-prices/update-from-api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/crypto-prices/update-from-api error en API retorna 400")
    void updateFromAPIError() throws Exception {
        String apiResponse = "{\"error\":\"API key inválida\"}";
        when(cryptoService.llamarAPIExterna("BTC", "USD")).thenReturn(apiResponse);

        Map<String, String> request = Map.of("symbol", "BTC", "market", "USD");

        mockMvc.perform(post("/api/crypto-prices/update-from-api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
