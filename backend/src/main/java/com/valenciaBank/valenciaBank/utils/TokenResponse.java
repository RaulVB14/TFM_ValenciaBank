package com.valenciaBank.valenciaBank.utils;

// Clase auxiliar para envolver el token en un objeto JSON
    public class TokenResponse {
    private String token;

    public TokenResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}