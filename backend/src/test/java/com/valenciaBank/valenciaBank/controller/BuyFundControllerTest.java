package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.model.FundPurchase;
import com.valenciaBank.valenciaBank.model.User;
import com.valenciaBank.valenciaBank.service.BuyFundService;
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

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuyFundController - Tests unitarios")
class BuyFundControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BuyFundService buyFundService;

    @InjectMocks
    private BuyFundController buyFundController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(buyFundController).build();
    }

    @Test
    @DisplayName("POST /fund/purchase/buy compra de fondo exitosa")
    void buyFundExitoso() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Compra realizada exitosamente");

        when(buyFundService.buyFund(eq(1L), eq("SPY"), eq("SPDR S&P 500"), eq("ETF"), eq(5.0), eq("USD")))
                .thenReturn(response);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("userId", 1);
        request.put("symbol", "SPY");
        request.put("name", "SPDR S&P 500");
        request.put("type", "ETF");
        request.put("quantity", 5.0);
        request.put("currency", "USD");

        mockMvc.perform(post("/fund/purchase/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /fund/purchase/sell venta de fondo exitosa")
    void sellFundExitoso() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Venta realizada");

        when(buyFundService.sellFund(eq(1L), eq(1L), eq(2.0))).thenReturn(response);

        Map<String, Object> request = Map.of(
                "userId", 1, "purchaseId", 1, "quantityToSell", 2.0
        );

        mockMvc.perform(post("/fund/purchase/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /fund/purchase/portfolio/{userId} retorna fondos del usuario")
    void getUserFunds() throws Exception {
        User user = new User();
        user.setId(1L);
        FundPurchase fund = new FundPurchase(user, "SPY", "SPDR S&P 500", "ETF", 10.0, 495.0, "USD");

        when(buyFundService.getUserFunds(1L)).thenReturn(List.of(fund));

        mockMvc.perform(get("/fund/purchase/portfolio/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.funds").isArray());
    }

    @Test
    @DisplayName("GET /fund/purchase/portfolio/detailed/{userId} retorna portafolio detallado")
    void getDetailedFundPortfolio() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("positions", List.of());

        when(buyFundService.getDetailedFundPortfolio(1L)).thenReturn(response);

        mockMvc.perform(get("/fund/purchase/portfolio/detailed/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
