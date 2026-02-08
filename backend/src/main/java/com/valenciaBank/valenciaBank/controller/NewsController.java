package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.model.NewsArticle;
import com.valenciaBank.valenciaBank.service.NewsScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsScraperService newsScraperService;

    /**
     * Obtener noticias de criptomonedas
     * GET /api/news/crypto
     */
    @GetMapping("/crypto")
    public ResponseEntity<List<NewsArticle>> getCryptoNews() {
        return ResponseEntity.ok(newsScraperService.getCryptoNews());
    }

    /**
     * Obtener noticias de economía
     * GET /api/news/economy
     */
    @GetMapping("/economy")
    public ResponseEntity<List<NewsArticle>> getEconomyNews() {
        return ResponseEntity.ok(newsScraperService.getEconomyNews());
    }

    /**
     * Obtener todas las noticias (crypto + economía)
     * GET /api/news/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<NewsArticle>> getAllNews() {
        return ResponseEntity.ok(newsScraperService.getAllNews());
    }
}
