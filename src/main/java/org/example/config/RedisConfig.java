package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Creates a Redis connection factory using default settings.
        // By default, this connects to localhost:6379.
        // This is sufficient for local development.
        // In production, the factory would typically be configured via
        // properties, Sentinel, or a Redis cluster.
        return new LettuceConnectionFactory();
    }

    //    @Bean
    //    public LettuceConnectionFactory redisConnectionFactory(
    //            RedisProperties redisProperties
    //    ) {
    //        return new LettuceConnectionFactory(
    //                redisProperties.getHost(),
    //                redisProperties.getPort()
    //        );
    //    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        // Exposes StringRedisTemplate for convenient string-based Redis operations.
        // Backed by the same RedisConnectionFactory above.
        return new StringRedisTemplate(factory);
    }
}
