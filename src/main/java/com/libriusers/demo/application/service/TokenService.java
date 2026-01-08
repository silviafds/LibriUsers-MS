package com.libriusers.demo.application.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.libriusers.demo.domain.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.auth0.jwt.algorithms.Algorithm;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;

    private Set<String> tokenBlacklist = new HashSet<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        // Limpa tokens expirados da blacklist a cada hora
        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.HOURS);
    }

    public String generateToken(User user) {
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("Libri-users")
                    .withSubject(user.getEmail())
                    .withClaim("userId", user.getId().toString())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
            return token;

        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while generating token", exception);
        }
    }

    // TokenService.java no User Service - CORRIGIDO
    public String validateToken(String token) {
        System.out.println("=== TOKEN SERVICE VALIDATE ===");
        System.out.println("Validating token: " +
                (token.length() > 30 ? token.substring(0, 30) + "..." : token));

        // Verifica blacklist primeiro
        if (tokenBlacklist.contains(token)) {
            System.out.println("❌ Token is in blacklist!");
            return "";
        }

        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            // Decodifica para ver o conteúdo
            DecodedJWT decodedJWT = JWT.decode(token);
            System.out.println("Decoded token:");
            System.out.println("  Subject: " + decodedJWT.getSubject());
            System.out.println("  Expires at: " + decodedJWT.getExpiresAt());
            System.out.println("  Issuer: " + decodedJWT.getIssuer());

            // Agora verifica
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("Libri-users")
                    .build();

            DecodedJWT verifiedJWT = verifier.verify(token);
            String subject = verifiedJWT.getSubject();

            System.out.println("✅ Token validated successfully");
            System.out.println("  Valid subject: " + subject);
            System.out.println("=== END TOKEN VALIDATION ===");

            return subject;

        } catch (TokenExpiredException e) {
            System.out.println("❌ Token EXPIRED: " + e.getMessage());
            return "";
        } catch (JWTVerificationException e) {
            System.out.println("❌ Token verification FAILED: " + e.getMessage());
            e.printStackTrace();
            return "";
        } catch (Exception e) {
            System.out.println("❌ UNEXPECTED error: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    public void addToBlacklist(String token) {
        System.out.println("Adding token to blacklist: " + token.substring(0, Math.min(20, token.length())) + "...");
        tokenBlacklist.add(token);
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }

    public void cleanupExpiredTokens() {
        System.out.println("Cleaning up expired tokens from blacklist...");
        int initialSize = tokenBlacklist.size();

        // Remove tokens expirados da blacklist
        tokenBlacklist.removeIf(token -> {
            try {
                Algorithm algorithm = Algorithm.HMAC256(secret);
                JWT.require(algorithm)
                        .withIssuer("Libri-users")
                        .build()
                        .verify(token);
                return false; // Token ainda válido, não remove
            } catch (JWTVerificationException e) {
                return true; // Token expirado, remove da blacklist
            }
        });

        System.out.println("Cleaned " + (initialSize - tokenBlacklist.size()) + " expired tokens from blacklist");
    }

    private Instant genExpirationDate() {
        //return LocalDateTime.now().plusMinutes(5).toInstant(ZoneOffset.of("-03:00"));
        return LocalDateTime.now().plusHours(8).toInstant(ZoneOffset.of("-03:00"));
    }

}
