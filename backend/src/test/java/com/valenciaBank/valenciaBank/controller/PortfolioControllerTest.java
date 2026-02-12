package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.model.CryptoPurchase;
import com.valenciaBank.valenciaBank.model.User;
import com.valenciaBank.valenciaBank.service.PortfolioService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioController - Tests unitarios")
class PortfolioControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PortfolioService portfolioService;

    @InjectMocks
    private PortfolioController portfolioController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(portfolioController).build();
    }

    @Test
    @DisplayName("GET /portfolio/user/{userId} retorna portafolio del usuario")
    void getPortfolioExitoso() throws Exception {
        User user = new User();
        user.setId(1L);
        CryptoPurchase purchase = new CryptoPurchase(user, "BTC", 0.5, 50000.0, "USD");
        when(portfolioService.getPortfolio(1L)).thenReturn(List.of(purchase));

        mockMvc.perform(get("/portfolio/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /portfolio/user/{userId} usuario no encontrado retorna 404")
    void getPortfolioNoEncontrado() throws Exception {
        when(portfolioService.getPortfolio(999L)).thenThrow(new RuntimeException("No encontrado"));

        mockMvc.perform(get("/portfolio/user/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /portfolio/user/{userId}/symbol/{symbol} retorna holdings")
    void getCryptoHoldings() throws Exception {
        User user = new User();
        user.setId(1L);
        CryptoPurchase purchase = new CryptoPurchase(user, "ETH", 10.0, 3000.0, "USD");
        when(portfolioService.getCryptoHoldings(1L, "ETH")).thenReturn(List.of(purchase));

        mockMvc.perform(get("/portfolio/user/1/symbol/ETH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /portfolio/user/{userId}/quantity/{symbol} retorna totales")
    void getTotalQuantity() throws Exception {
        when(portfolioService.getTotalQuantity(1L, "BTC")).thenReturn(1.5);
        when(portfolioService.getAverageCost(1L, "BTC")).thenReturn(45000.0);

        mockMvc.perform(get("/portfolio/user/1/quantity/BTC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("BTC"))
                .andExpect(jsonPath("$.totalQuantity").value(1.5))
                .andExpect(jsonPath("$.averageCost").value(45000.0));
    }

    @Test
    @DisplayName("POST /portfolio/add añade compra de crypto")
    void addCryptoPurchase() throws Exception {
        User user = new User();
        user.setId(1L);
        CryptoPurchase purchase = new CryptoPurchase(user, "SOL", 100.0, 150.0, "USD");
        when(portfolioService.addCryptoPurchase(eq(1L), eq("SOL"), eq(100.0), eq(150.0), eq("USD")))
                .thenReturn(purchase);

        Map<String, Object> request = Map.of(
                "userId", 1, "symbol", "SOL", "quantity", 100.0,
                "purchasePrice", 150.0, "currency", "USD"
        );

        mockMvc.perform(post("/portfolio/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /portfolio/{purchaseId} elimina compra")
    void removeCryptoPurchase() throws Exception {
        doNothing().when(portfolioService).removeCryptoPurchase(1L);

        mockMvc.perform(delete("/portfolio/1"))
                .andExpect(status().isNoContent());

        verify(portfolioService).removeCryptoPurchase(1L);
    }

    @Test
    @DisplayName("DELETE /portfolio/{purchaseId} no encontrado retorna 404")
    void removeCryptoPurchaseNoEncontrado() throws Exception {
        doThrow(new RuntimeException("No encontrado")).when(portfolioService).removeCryptoPurchase(999L);

        mockMvc.perform(delete("/portfolio/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /portfolio/{purchaseId}/quantity actualiza cantidad")
    void updateQuantity() throws Exception {
        User user = new User();
        user.setId(1L);
        CryptoPurchase updated = new CryptoPurchase(user, "BTC", 2.0, 50000.0, "USD");
        when(portfolioService.updateQuantity(1L, 2.0)).thenReturn(updated);

        mockMvc.perform(put("/portfolio/1/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2.0}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /portfolio/detailed/{userId} retorna portafolio detallado")
    void getDetailedPortfolio() throws Exception {
        Map<String, Object> portfolio = Map.of(
                "success", true, "totalValue", 100000.0, "positions", List.of()
        );
        when(portfolioService.getDetailedPortfolio(1L)).thenReturn(portfolio);

        mockMvc.perform(get("/portfolio/detailed/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /portfolio/history/{userId} retorna historial")
    void getPortfolioHistory() throws Exception {
        Map<String, Object> history = Map.of("success", true, "days", 30, "data", List.of());
        when(portfolioService.getPortfolioHistory(1L, 30)).thenReturn(history);

        mockMvc.perform(get("/portfolio/history/1").param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /portfolio/detailed/{userId} error retorna 400")
    void getDetailedPortfolioError() throws Exception {
        when(portfolioService.getDetailedPortfolio(999L))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/portfolio/detailed/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
