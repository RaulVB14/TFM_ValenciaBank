package com.valenciaBank.valenciaBank.model;

public class NewsArticle {
    private String title;
    private String description;
    private String url;
    private String imageUrl;
    private String source;
    private String category; // "crypto" o "economy"
    private String publishedAt;

    public NewsArticle() {}

    public NewsArticle(String title, String description, String url, String imageUrl, String source, String category, String publishedAt) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.imageUrl = imageUrl;
        this.source = source;
        this.category = category;
        this.publishedAt = publishedAt;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
}
