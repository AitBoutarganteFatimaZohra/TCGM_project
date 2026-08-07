package com.tcgm.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret:defaultSecretKeyForTCGMProject2024WithMoreThan32Characters}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}") // 24h par défaut
    private long jwtExpiration;

    @Value("${app.jwt.refresh-expiration:604800000}") // 7 jours par défaut
    private long refreshExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Créer la clé à partir du secret
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT Token Provider initialisé avec succès");
    }

    /**
     * Génère un token d'accès
     */
    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername(), jwtExpiration);
    }

    /**
     * Génère un token d'accès à partir de l'email
     */
    public String generateAccessToken(String email) {
        return generateToken(email, jwtExpiration);
    }

    /**
     * Génère un token de rafraîchissement
     */
    public String generateRefreshToken(String email) {
        return generateToken(email, refreshExpiration);
    }

    /**
     * Génère un token JWT
     */
    private String generateToken(String email, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact();
    }

    /**
     * Extrait l'email du token
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claims.getSubject();
    }

    /**
     * Valide le token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Token JWT invalide: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Token JWT expiré: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT non supporté: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Token JWT vide ou invalide: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Récupère la durée d'expiration du token d'accès
     */
    public long getAccessTokenExpiration() {
        return jwtExpiration;
    }

    /**
     * Récupère la durée d'expiration du token de rafraîchissement
     */
    public long getRefreshTokenExpiration() {
        return refreshExpiration;
    }
}