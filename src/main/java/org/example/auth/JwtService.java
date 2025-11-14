package org.example.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    // Access Token lifetime (minutes)
    @Value("${jwt.access-ttl-minutes:15}")
    private long accessTtlMinutes;

    // Refresh Token lifetime (days)
    @Value("${jwt.refresh-ttl-days:7}")
    private long refreshTtlDays;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ============================
    //        ACCESS TOKEN
    // ============================
    public String generateAccessToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(Map.of("typ", "access"))
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(accessTtlMinutes, ChronoUnit.MINUTES)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ============================
    //        REFRESH TOKEN
    // ============================
    public String generateRefreshToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(Map.of("typ", "refresh"))
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(refreshTtlDays, ChronoUnit.DAYS)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ============================
    //      TOKEN UTILITIES
    // ============================

    public String parseUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenExpired(String token) {
        try {
            Date exp = extractAllClaims(token).getExpiration();
            return exp.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isTokenType(String token, String expectedType) {
        try {
            Object typ = extractAllClaims(token).get("typ");
            return expectedType.equals(typ);
        } catch (Exception e) {
            return false;
        }
    }

    public Long extractExpirationMillis(String token) {
        try {
            Date exp = extractAllClaims(token).getExpiration();
            return exp != null ? exp.getTime() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
