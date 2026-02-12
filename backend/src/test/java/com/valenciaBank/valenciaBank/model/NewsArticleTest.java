package com.valenciaBank.valenciaBank.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NewsArticle - Tests unitarios del modelo")
class NewsArticleTest {

    @Test
    @DisplayName("Constructor con parámetros asigna todos los valores")
    void constructorConParametros() {
        NewsArticle article = new NewsArticle(
                "Bitcoin sube un 10%",
                "El precio de Bitcoin aumenta significativamente",
                "https://example.com/news/1",
                "https://example.com/img/1.jpg",
                "CoinTelegraph",
                "crypto",
                "2026-02-12"
        );

        assertEquals("Bitcoin sube un 10%", article.getTitle());
        assertEquals("El precio de Bitcoin aumenta significativamente", article.getDescription());
        assertEquals("https://example.com/news/1", article.getUrl());
        assertEquals("https://example.com/img/1.jpg", article.getImageUrl());
        assertEquals("CoinTelegraph", article.getSource());
        assertEquals("crypto", article.getCategory());
        assertEquals("2026-02-12", article.getPublishedAt());
    }

    @Test
    @DisplayName("Constructor vacío y setters funcionan")
    void constructorVacioYSetters() {
        NewsArticle article = new NewsArticle();
        article.setTitle("Economía crece");
        article.setDescription("La economía europea crece un 2%");
        article.setUrl("https://example.com/news/2");
        article.setImageUrl("https://example.com/img/2.jpg");
        article.setSource("Investing.com");
        article.setCategory("economy");
        article.setPublishedAt("2026-02-11");

        assertEquals("Economía crece", article.getTitle());
        assertEquals("economy", article.getCategory());
        assertEquals("Investing.com", article.getSource());
    }
}
