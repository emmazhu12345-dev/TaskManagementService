package org.example.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.example.model.AppUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CaffeineConfig {

    @Bean
    public Cache<String, AppUser> userByUsernameCache() {
        return Caffeine.newBuilder()
                .maximumSize(10_000) // avoid unbounded memory
                .expireAfterWrite(10, TimeUnit.MINUTES) // TTL for user entries
                .build();
    }
}
