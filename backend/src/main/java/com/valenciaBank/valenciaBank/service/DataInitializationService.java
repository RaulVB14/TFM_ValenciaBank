package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.CryptoPrice;
import com.valenciaBank.valenciaBank.repository.CryptoPriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DataInitializationService {

    private static final Logger log = LoggerFactory.getLogger(DataInitializationService.class);

    @Autowired
    private CryptoPriceRepository cryptoPriceRepository;

    /**
     * Inicializar precios de criptomonedas al arrancar la aplicación
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeCryptoPrices() {
        log.info("Inicializando precios de criptomonedas...");

        String[] symbols = {"BTC", "ETH", "ADA", "SOL", "XRP", "DOT", "DOGE", "LINK", "MATIC", "UNI", 
                           "AVAX", "LTC", "BCH", "ETC", "XLM", "ATOM", "NEAR", "FLOW", "THETA", "VET"};
        
        double[] pricesEUR = {95000.00, 3500.00, 1.20, 210.00, 3.50, 40.00, 0.35, 28.50, 1.10, 18.00,
                             85.00, 200.00, 550.00, 45.00, 0.45, 15.50, 8.20, 5.75, 6.80, 0.12};

        for (int i = 0; i < symbols.length; i++) {
            String symbol = symbols[i];
            String market = "EUR";
            double price = pricesEUR[i];

            Optional<CryptoPrice> existing = cryptoPriceRepository.findBySymbolAndMarket(symbol, market);
            if (existing.isEmpty()) {
                CryptoPrice cryptoPrice = new CryptoPrice(symbol, market, price);
                cryptoPriceRepository.save(cryptoPrice);
                log.info("Precio inicializado: {} = {} {}", symbol, price, market);
            }
        }

        log.info("Inicialización de precios completada");
    }
}
