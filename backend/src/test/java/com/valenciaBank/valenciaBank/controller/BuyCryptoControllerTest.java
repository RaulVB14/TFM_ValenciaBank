package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.service.BuyCryptoService;
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

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuyCryptoController - Tests unitarios")
class BuyCryptoControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BuyCryptoService buyCryptoService;

    @InjectMocks
    private BuyCryptoController buyCryptoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(buyCryptoController).build();
    }

    @Test
    @DisplayName("POST /crypto/purchase/buy compra exitosa")
    void buyCryptoExitoso() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Compra realizada");

        when(buyCryptoService.buyCrypto(eq(1L), eq("BTC"), eq(0.5), eq("EUR")))
                .thenReturn(result);

        Map<String, Object> request = Map.of(
                "userId", 1, "symbol", "BTC", "quantity", 0.5, "market", "EUR"
        );

        mockMvc.perform(post("/crypto/purchase/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /crypto/purchase/buy saldo insuficiente retorna 400")
    void buyCryptoSaldoInsuficiente() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", "Saldo insuficiente");

        when(buyCryptoService.buyCrypto(eq(1L), eq("BTC"), eq(10.0), eq("EUR")))
                .thenReturn(result);

        Map<String, Object> request = Map.of(
                "userId", 1, "symbol", "BTC", "quantity", 10.0, "market", "EUR"
        );

        mockMvc.perform(post("/crypto/purchase/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /crypto/purchase/sell venta exitosa")
    void sellCryptoExitoso() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Venta realizada");

        when(buyCryptoService.sellCrypto(eq(1L), eq(1L), eq(0.5)))
                .thenReturn(result);

        Map<String, Object> request = Map.of(
                "userId", 1, "purchaseId", 1, "quantityToSell", 0.5
        );

        mockMvc.perform(post("/crypto/purchase/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /crypto/purchase/buy formato inválido retorna 400")
    void buyCryptoFormatoInvalido() throws Exception {
        mockMvc.perform(post("/crypto/purchase/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"invalid\":\"data\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
