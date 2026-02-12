package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.repository.CryptoHistoryCacheRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CoinGeckoService - Tests unitarios")
class CoinGeckoServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CryptoHistoryCacheRepository cacheRepository;

    @InjectMocks
    private CoinGeckoService coinGeckoService;

    @Test
    @DisplayName("convertSymbolToCoinGeckoId convierte BTC correctamente")
    void convertBTC() {
        assertEquals("bitcoin", coinGeckoService.convertSymbolToCoinGeckoId("BTC"));
    }

    @Test
    @DisplayName("convertSymbolToCoinGeckoId convierte ETH correctamente")
    void convertETH() {
        assertEquals("ethereum", coinGeckoService.convertSymbolToCoinGeckoId("ETH"));
    }

    @Test
    @DisplayName("convertSymbolToCoinGeckoId convierte SOL correctamente")
    void convertSOL() {
        assertEquals("solana", coinGeckoService.convertSymbolToCoinGeckoId("SOL"));
    }

    @Test
    @DisplayName("convertSymbolToCoinGeckoId convierte ADA correctamente")
    void convertADA() {
        assertEquals("cardano", coinGeckoService.convertSymbolToCoinGeckoId("ADA"));
    }

    @Test
    @DisplayName("convertSymbolToCoinGeckoId convierte XRP correctamente")
    void convertXRP() {
        assertEquals("ripple", coinGeckoService.convertSymbolToCoinGeckoId("XRP"));
    }

    @Test
    @DisplayName("convertSymbolToCoinGeckoId convierte DOGE correctamente")
    void convertDOGE() {
        assertEquals("dogecoin", coinGeckoService.convertSymbolToCoinGeckoId("DOGE"));
    }

    @Test
    @DisplayName("convertSymbolToCoinGeckoId es case-insensitive")
    void convertCaseInsensitive() {
        assertEquals("bitcoin", coinGeckoService.convertSymbolToCoinGeckoId("btc"));
        assertEquals("ethereum", coinGeckoService.convertSymbolToCoinGeckoId("eth"));
    }

    @Test
    @DisplayName("convertSymbolToCoinGeckoId retorna lowercase para desconocidos")
    void convertDesconocido() {
        assertEquals("unknown", coinGeckoService.convertSymbolToCoinGeckoId("UNKNOWN"));
    }

    @Test
    @DisplayName("convertSymbolToCoinGeckoId mapea stablecoins correctamente")
    void convertStablecoins() {
        assertEquals("tether", coinGeckoService.convertSymbolToCoinGeckoId("USDT"));
        assertEquals("usd-coin", coinGeckoService.convertSymbolToCoinGeckoId("USDC"));
    }

    @Test
    @DisplayName("convertSymbolToCoinGeckoId mapea todos los DeFi tokens")
    void convertDeFiTokens() {
        assertEquals("uniswap", coinGeckoService.convertSymbolToCoinGeckoId("UNI"));
        assertEquals("aave", coinGeckoService.convertSymbolToCoinGeckoId("AAVE"));
        assertEquals("chainlink", coinGeckoService.convertSymbolToCoinGeckoId("LINK"));
    }
}
