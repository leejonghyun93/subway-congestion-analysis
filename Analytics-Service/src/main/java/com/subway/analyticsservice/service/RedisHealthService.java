package com.subway.analyticsservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisHealthService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis 연결 상태 확인
     */
    public boolean isRedisAvailable() {
        try {
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            log.info("Redis health check: {}", pong);
            return "PONG".equals(pong);
        } catch (Exception e) {
            log.error("Redis is not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Redis 캐시 클리어 (특정 패턴)
     */
    public void clearCache(String pattern) {
        try {
            redisTemplate.keys(pattern + "*")
                    .forEach(key -> redisTemplate.delete(key));
            log.info("Cleared cache for pattern: {}", pattern);
        } catch (Exception e) {
            log.error("Failed to clear cache: {}", e.getMessage());
        }
    }

    /**
     * 전체 캐시 클리어
     */
    public void clearAllCache() {
        try {
            redisTemplate.getConnectionFactory()
                    .getConnection()
                    .flushAll();
            log.info("Cleared all cache");
        } catch (Exception e) {
            log.error("Failed to clear all cache: {}", e.getMessage());
        }
    }
}
