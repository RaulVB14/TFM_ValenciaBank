package com.valenciaBank.valenciaBank.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class Jwt {

    private static String secretKey;

    private static final long EXPIRATION_TIME = 86400000;

    @Value("${jwt.key}")
    public void setSecretKey(String key) {
        Jwt.secretKey = key;
    }

    public static String generateToken(String dni) {
        return JWT.create()
                .withSubject(dni)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(secretKey));
    }

    public static String validateToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secretKey))
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}


