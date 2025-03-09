package com.lynxysservicetemplate.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Date;

@Component
public class JwtTokenUtil {

    @Value("${spring.jwt.secret_key}")
    private String secretKey;

    @Value("${spring.jwt.accessTokenValiditySeconds}")
    private long accessTokenValiditySeconds;

    @Value("${spring.jwt.refreshTokenValiditySeconds}")
    private long refreshTokenValiditySeconds;

    public String generateAccessToken(String email) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            return JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(email)
                    .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenValiditySeconds * 1000))
                    .sign(algorithm);
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException(exception.getMessage());
        }
    }

    public String generateRefreshToken(String email) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            return JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(email)
                    .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenValiditySeconds * 1000))
                    .sign(algorithm);
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException(exception.getMessage());
        }
    }

    public boolean validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public String extractEmail(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .build()
                    .verify(token);

            return decodedJWT.getSubject();
        } catch (Exception exception) {
            return "";
        }
    }

}
