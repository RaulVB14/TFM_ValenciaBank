package com.valenciaBank.valenciaBank.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class Jwt {

    private static final String SECRET_KEY = "tontosilolees";

    private static final long EXPIRATION_TIME = 86400000;

    public static String generateToken(String dni) {
        return JWT.create()
                .withSubject(dni)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    public static String validateToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(getSecretStatic())) //use of static method.
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    private static String getSecretStatic(){
        //this is a workaround, as the @Value annotation does not work in static context.
        //it should be improved.
        Jwt jwt = new Jwt();
        return SECRET_KEY;
    }
}


