package com.valenciaBank.valenciaBank.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.StreamWriteConstraints;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración personalizada de Jackson para aumentar los límites de anidamiento
 * y mejorar el manejo de respuestas complejas de APIs externas
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Aumentar el límite de profundidad de anidamiento a 5000
        // (default es 1000, lo cual causa problemas con respuestas grandes de CoinGecko/Finnhub)
        mapper.getFactory().setStreamWriteConstraints(
            StreamWriteConstraints.builder()
                .maxNestingDepth(5000)
                .build()
        );
        
        return mapper;
    }
}
