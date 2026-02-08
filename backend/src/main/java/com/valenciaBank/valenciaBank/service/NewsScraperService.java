package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.NewsArticle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NewsScraperService {

    // Cache simple: clave -> lista de noticias + timestamp
    private final ConcurrentHashMap<String, CachedNews> cache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 15 * 60 * 1000; // 15 minutos

    /**
     * Obtener noticias crypto desde CoinDesk
     */
    public List<NewsArticle> getCryptoNews() {
        String cacheKey = "crypto";
        CachedNews cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.articles;
        }

        List<NewsArticle> articles = new ArrayList<>();
        try {
            // Scraping de CoinTelegraph (noticias crypto)
            Document doc = Jsoup.connect("https://cointelegraph.com/tags/bitcoin")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();

            Elements articleElements = doc.select("article, .post-card, .post-card-inline");
            for (Element el : articleElements) {
                if (articles.size() >= 8) break;
                try {
                    Element linkEl = el.selectFirst("a[href]");
                    Element titleEl = el.selectFirst("a.post-card-inline__title-link, h2, .post-card__title a, a.post-card__title-link");
                    Element imgEl = el.selectFirst("img[src], img[data-src]");
                    Element descEl = el.selectFirst("p, .post-card__text, .post-card-inline__text");

                    String title = titleEl != null ? titleEl.text().trim() : (linkEl != null ? linkEl.text().trim() : "");
                    String url = linkEl != null ? linkEl.absUrl("href") : "";
                    String img = imgEl != null ? (imgEl.hasAttr("data-src") ? imgEl.attr("data-src") : imgEl.absUrl("src")) : "";
                    String desc = descEl != null ? descEl.text().trim() : "";

                    if (!title.isEmpty() && title.length() > 10) {
                        if (desc.length() > 200) desc = desc.substring(0, 200) + "...";
                        articles.add(new NewsArticle(title, desc, url, img, "CoinTelegraph", "crypto", ""));
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            System.err.println("Error scraping CoinTelegraph: " + e.getMessage());
        }

        // Fallback: CoinDesk RSS si CoinTelegraph falla
        if (articles.isEmpty()) {
            try {
                Document doc = Jsoup.connect("https://www.coindesk.com/arc/outboundfeeds/rss/")
                        .userAgent("Mozilla/5.0")
                        .timeout(10000)
                        .get();

                Elements items = doc.select("item");
                for (Element item : items) {
                    if (articles.size() >= 8) break;
                    String title = item.selectFirst("title") != null ? item.selectFirst("title").text() : "";
                    String desc = item.selectFirst("description") != null ? item.selectFirst("description").text() : "";
                    String link = item.selectFirst("link") != null ? item.selectFirst("link").text() : "";
                    String pubDate = item.selectFirst("pubDate") != null ? item.selectFirst("pubDate").text() : "";

                    // Limpiar HTML de la descripción
                    desc = Jsoup.parse(desc).text();
                    if (desc.length() > 200) desc = desc.substring(0, 200) + "...";

                    Element mediaEl = item.selectFirst("media|content, media|thumbnail, enclosure");
                    String img = mediaEl != null ? mediaEl.attr("url") : "";

                    if (!title.isEmpty()) {
                        articles.add(new NewsArticle(title, desc, link, img, "CoinDesk", "crypto", pubDate));
                    }
                }
            } catch (Exception e) {
                System.err.println("Error scraping CoinDesk RSS: " + e.getMessage());
            }
        }

        cache.put(cacheKey, new CachedNews(articles));
        return articles;
    }

    /**
     * Obtener noticias de economía desde Reuters o ElEconomista
     */
    public List<NewsArticle> getEconomyNews() {
        String cacheKey = "economy";
        CachedNews cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.articles;
        }

        List<NewsArticle> articles = new ArrayList<>();

        // Scraping de Investing.com RSS (noticias de economía en español)
        try {
            Document doc = Jsoup.connect("https://es.investing.com/rss/news.rss")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();

            Elements items = doc.select("item");
            for (Element item : items) {
                if (articles.size() >= 8) break;
                String title = item.selectFirst("title") != null ? item.selectFirst("title").text() : "";
                String desc = item.selectFirst("description") != null ? item.selectFirst("description").text() : "";
                String link = item.selectFirst("link") != null ? item.selectFirst("link").text() : "";
                String pubDate = item.selectFirst("pubDate") != null ? item.selectFirst("pubDate").text() : "";

                desc = Jsoup.parse(desc).text();
                if (desc.length() > 200) desc = desc.substring(0, 200) + "...";

                Element mediaEl = item.selectFirst("media|content, media|thumbnail, enclosure");
                String img = mediaEl != null ? mediaEl.attr("url") : "";

                if (!title.isEmpty()) {
                    articles.add(new NewsArticle(title, desc, link, img, "Investing.com", "economy", pubDate));
                }
            }
        } catch (Exception e) {
            System.err.println("Error scraping Investing.com RSS: " + e.getMessage());
        }

        // Fallback: CNBC RSS
        if (articles.isEmpty()) {
            try {
                Document doc = Jsoup.connect("https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10001147")
                        .userAgent("Mozilla/5.0")
                        .timeout(10000)
                        .get();

                Elements items = doc.select("item");
                for (Element item : items) {
                    if (articles.size() >= 8) break;
                    String title = item.selectFirst("title") != null ? item.selectFirst("title").text() : "";
                    String desc = item.selectFirst("description") != null ? item.selectFirst("description").text() : "";
                    String link = item.selectFirst("link") != null ? item.selectFirst("link").text() : "";
                    String pubDate = item.selectFirst("pubDate") != null ? item.selectFirst("pubDate").text() : "";

                    desc = Jsoup.parse(desc).text();
                    if (desc.length() > 200) desc = desc.substring(0, 200) + "...";

                    Element mediaEl = item.selectFirst("media|content, media|thumbnail, enclosure");
                    String img = mediaEl != null ? mediaEl.attr("url") : "";

                    if (!title.isEmpty()) {
                        articles.add(new NewsArticle(title, desc, link, img, "CNBC", "economy", pubDate));
                    }
                }
            } catch (Exception e) {
                System.err.println("Error scraping CNBC RSS: " + e.getMessage());
            }
        }

        cache.put(cacheKey, new CachedNews(articles));
        return articles;
    }

    /**
     * Obtener todas las noticias (crypto + economía)
     */
    public List<NewsArticle> getAllNews() {
        List<NewsArticle> all = new ArrayList<>();
        all.addAll(getCryptoNews());
        all.addAll(getEconomyNews());
        return all;
    }

    // Clase interna para cache
    private static class CachedNews {
        List<NewsArticle> articles;
        long timestamp;

        CachedNews(List<NewsArticle> articles) {
            this.articles = articles;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }
}
