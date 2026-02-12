package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.model.NewsArticle;
import com.valenciaBank.valenciaBank.service.NewsScraperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NewsController - Tests unitarios")
class NewsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NewsScraperService newsScraperService;

    @InjectMocks
    private NewsController newsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(newsController).build();
    }

    private NewsArticle crearArticulo(String title, String url) {
        NewsArticle article = new NewsArticle();
        article.setTitle(title);
        article.setUrl(url);
        article.setSource("TestSource");
        article.setCategory("crypto");
        return article;
    }

    @Test
    @DisplayName("GET /api/news/crypto retorna noticias de crypto")
    void getCryptoNews() throws Exception {
        List<NewsArticle> noticias = List.of(
                crearArticulo("Bitcoin sube", "https://example.com/1"),
                crearArticulo("Ethereum se recupera", "https://example.com/2")
        );
        when(newsScraperService.getCryptoNews()).thenReturn(noticias);

        mockMvc.perform(get("/api/news/crypto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Bitcoin sube"));
    }

    @Test
    @DisplayName("GET /api/news/economy retorna noticias de economía")
    void getEconomyNews() throws Exception {
        List<NewsArticle> noticias = List.of(
                crearArticulo("PIB crece", "https://example.com/3")
        );
        when(newsScraperService.getEconomyNews()).thenReturn(noticias);

        mockMvc.perform(get("/api/news/economy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/news/all retorna todas las noticias")
    void getAllNews() throws Exception {
        List<NewsArticle> noticias = List.of(
                crearArticulo("Noticia crypto", "https://example.com/1"),
                crearArticulo("Noticia economía", "https://example.com/2"),
                crearArticulo("Otra noticia", "https://example.com/3")
        );
        when(newsScraperService.getAllNews()).thenReturn(noticias);

        mockMvc.perform(get("/api/news/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("GET /api/news/crypto lista vacía retorna array vacío")
    void getCryptoNewsVacio() throws Exception {
        when(newsScraperService.getCryptoNews()).thenReturn(List.of());

        mockMvc.perform(get("/api/news/crypto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
