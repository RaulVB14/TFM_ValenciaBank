package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.CryptoPurchase;
import com.valenciaBank.valenciaBank.model.User;
import com.valenciaBank.valenciaBank.repository.CryptoPriceRepository;
import com.valenciaBank.valenciaBank.repository.CryptoPurchaseRepository;
import com.valenciaBank.valenciaBank.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioService - Tests unitarios")
class PortfolioServiceTest {

    @Mock
    private CryptoPurchaseRepository cryptoPurchaseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CoinGeckoService coinGeckoService;

    @Mock
    private CryptoPriceRepository cryptoPriceRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    private User crearUsuarioTest() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setDni("12345678A");
        return user;
    }

    @Test
    @DisplayName("addCryptoPurchase crea compra correctamente")
    void addCryptoPurchaseExitoso() {
        User user = crearUsuarioTest();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cryptoPurchaseRepository.save(any(CryptoPurchase.class))).thenAnswer(inv -> {
            CryptoPurchase p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        CryptoPurchase purchase = portfolioService.addCryptoPurchase(1L, "BTC", 0.5, 95000.0, "EUR");

        assertNotNull(purchase);
        assertEquals("BTC", purchase.getSymbol());
        assertEquals(0.5, purchase.getQuantity());
        assertEquals(95000.0, purchase.getPurchasePrice());
        verify(cryptoPurchaseRepository).save(any(CryptoPurchase.class));
    }

    @Test
    @DisplayName("addCryptoPurchase lanza excepción si usuario no existe")
    void addCryptoPurchaseUsuarioNoExiste() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> portfolioService.addCryptoPurchase(999L, "BTC", 0.5, 95000.0, "EUR"));

        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    @Test
    @DisplayName("getPortfolio retorna lista de compras del usuario")
    void getPortfolio() {
        User user = crearUsuarioTest();
        CryptoPurchase p1 = new CryptoPurchase(user, "BTC", 0.5, 95000.0, "EUR");
        CryptoPurchase p2 = new CryptoPurchase(user, "ETH", 2.0, 3500.0, "EUR");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cryptoPurchaseRepository.findByUser(user)).thenReturn(Arrays.asList(p1, p2));

        List<CryptoPurchase> portfolio = portfolioService.getPortfolio(1L);

        assertEquals(2, portfolio.size());
    }

    @Test
    @DisplayName("getCryptoHoldings retorna posiciones de un símbolo")
    void getCryptoHoldings() {
        User user = crearUsuarioTest();
        CryptoPurchase p = new CryptoPurchase(user, "BTC", 0.5, 95000.0, "EUR");

        when(cryptoPurchaseRepository.findByUserIdAndSymbol(1L, "BTC")).thenReturn(List.of(p));

        List<CryptoPurchase> holdings = portfolioService.getCryptoHoldings(1L, "BTC");

        assertEquals(1, holdings.size());
        assertEquals("BTC", holdings.get(0).getSymbol());
    }

    @Test
    @DisplayName("getTotalQuantity suma cantidades correctamente")
    void getTotalQuantity() {
        User user = crearUsuarioTest();
        CryptoPurchase p1 = new CryptoPurchase(user, "BTC", 0.3, 90000.0, "EUR");
        CryptoPurchase p2 = new CryptoPurchase(user, "BTC", 0.2, 95000.0, "EUR");

        when(cryptoPurchaseRepository.findByUserIdAndSymbol(1L, "BTC")).thenReturn(Arrays.asList(p1, p2));

        Double total = portfolioService.getTotalQuantity(1L, "BTC");

        assertEquals(0.5, total, 0.001);
    }

    @Test
    @DisplayName("getAverageCost calcula coste medio ponderado")
    void getAverageCost() {
        User user = crearUsuarioTest();
        CryptoPurchase p1 = new CryptoPurchase(user, "BTC", 0.3, 90000.0, "EUR"); // 27000
        CryptoPurchase p2 = new CryptoPurchase(user, "BTC", 0.2, 95000.0, "EUR"); // 19000

        when(cryptoPurchaseRepository.findByUserIdAndSymbol(1L, "BTC")).thenReturn(Arrays.asList(p1, p2));

        Double avgCost = portfolioService.getAverageCost(1L, "BTC");

        // Total cost: 27000 + 19000 = 46000, Total qty: 0.5, Avg: 92000
        assertEquals(92000.0, avgCost, 0.01);
    }

    @Test
    @DisplayName("getAverageCost retorna 0 si no hay compras")
    void getAverageCostSinCompras() {
        when(cryptoPurchaseRepository.findByUserIdAndSymbol(1L, "BTC")).thenReturn(List.of());

        Double avgCost = portfolioService.getAverageCost(1L, "BTC");

        assertEquals(0.0, avgCost);
    }

    @Test
    @DisplayName("removeCryptoPurchase elimina compra existente")
    void removeCryptoPurchase() {
        when(cryptoPurchaseRepository.existsById(1L)).thenReturn(true);

        portfolioService.removeCryptoPurchase(1L);

        verify(cryptoPurchaseRepository).deleteById(1L);
    }

    @Test
    @DisplayName("removeCryptoPurchase lanza excepción si no existe")
    void removeCryptoPurchaseNoExiste() {
        when(cryptoPurchaseRepository.existsById(999L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> portfolioService.removeCryptoPurchase(999L));

        assertEquals("Compra no encontrada", ex.getMessage());
    }

    @Test
    @DisplayName("updateQuantity actualiza cantidad correctamente")
    void updateQuantity() {
        CryptoPurchase purchase = new CryptoPurchase(crearUsuarioTest(), "BTC", 0.5, 95000.0, "EUR");
        purchase.setId(1L);

        when(cryptoPurchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        when(cryptoPurchaseRepository.save(any())).thenReturn(purchase);

        CryptoPurchase updated = portfolioService.updateQuantity(1L, 0.3);

        assertEquals(0.3, updated.getQuantity());
    }

    @Test
    @DisplayName("updateQuantity elimina compra si nueva cantidad es 0 o negativa")
    void updateQuantityCero() {
        CryptoPurchase purchase = new CryptoPurchase(crearUsuarioTest(), "BTC", 0.5, 95000.0, "EUR");
        purchase.setId(1L);

        when(cryptoPurchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));

        CryptoPurchase result = portfolioService.updateQuantity(1L, 0.0);

        assertNull(result);
        verify(cryptoPurchaseRepository).deleteById(1L);
    }
}
