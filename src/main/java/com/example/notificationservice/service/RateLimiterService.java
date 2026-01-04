package com.example.notificationservice.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Allows max 5 requests per user per minute.
     */
    public boolean allowRequest(Long userId) {
        String key = "rate:user:" + userId;

        // Increment request count
        Long count = redisTemplate.opsForValue().increment(key);

        // Set expiry only on first request
        if (count != null && count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }

        // Allow only first 5 requests
        return count != null && count <= 5;
    }
}
