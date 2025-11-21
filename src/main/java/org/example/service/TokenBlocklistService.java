package org.example.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.example.auth.JwtService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlocklistService {

    private final StringRedisTemplate redis;
    private final JwtService jwtService;

    public void blockToken(String token, long expiresAtMillis) {
        String username = jwtService.parseUsername(token);
        String jti = jwtService.extractJti(token);

        if (jti == null || jti.isBlank()) {
            return;
        }

        long now = System.currentTimeMillis();
        long ttlMillis = expiresAtMillis - now;
        if (ttlMillis <= 0) {
            return;
        }

        String key = buildRedisKey(username, jti);

        redis.opsForValue().set(key, String.valueOf(expiresAtMillis), Duration.ofMillis(ttlMillis));
    }

    public boolean isBlocked(String token) {
        String username = jwtService.parseUsername(token);
        String jti = jwtService.extractJti(token);

        if (jti == null || jti.isBlank()) {
            return false;
        }

        String key = buildRedisKey(username, jti);
        Boolean exists = redis.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    private String buildRedisKey(String username, String jti) {
        String userPart = (username == null || username.isBlank()) ? "anonymous" : username;
        return "token:blocklist:" + userPart + ":" + jti;
    }
}
