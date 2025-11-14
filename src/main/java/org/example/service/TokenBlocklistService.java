package org.example.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of JWT tokens that have been explicitly blocked (logged out or revoked).
 */
@Service
@RequiredArgsConstructor
public class TokenBlocklistService {

    // token -> expiration timestamp (epoch millis)
    private final Map<String, Long> blocklist = new ConcurrentHashMap<>();

    /**
     * Add token to blocklist with its expiration time.
     */
    public void blockToken(String token, long expiresAtMillis) {
        blocklist.put(token, expiresAtMillis);
    }

    /**
     * Check whether the token is blocked.
     * Also removes expired entries to avoid memory leaks.
     */
    public boolean isBlocked(String token) {
        Long exp = blocklist.get(token);
        if (exp == null) return false;
        if (Instant.now().toEpochMilli() >= exp) {
            blocklist.remove(token);
            return false;
        }
        return true;
    }
}
