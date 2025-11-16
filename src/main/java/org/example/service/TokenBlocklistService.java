package org.example.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Keeps track of JWT tokens that have been explicitly blocked (logged out or revoked).
 */
@Service
@RequiredArgsConstructor
public class TokenBlocklistService {

    private final StringRedisTemplate redis;

    /**
     * Add token to blocklist with its expiration time.
     * Redis will auto-expire the token key.
     */
    public void blockToken(String token, long expiresAtMillis) {
        long now = System.currentTimeMillis();
        long ttlMillis = expiresAtMillis - now;
        if (ttlMillis <= 0) {
            return; // already expired, no need to store
        }

        String key = buildRedisKey(token);

        // value = expiration timestamp (optional, but keeps consistent with old interface)
        redis.opsForValue().set(key, String.valueOf(expiresAtMillis),
                Duration.ofMillis(ttlMillis));
    }

    /**
     * Check if token is in blocklist.
     * We don't need to manually cleanup; Redis auto-expires.
     */
    public boolean isBlocked(String token) {
        String key = buildRedisKey(token);
        String val = redis.opsForValue().get(key);
        return val != null;  // if key exists → token is blocked
    }

    private String buildRedisKey(String token) {
        return "blocklist:" + token;
    }
}
