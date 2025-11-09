package com.subway.analyticsservice.controller;

import com.subway.analyticsservice.dto.ApiResponse;
import com.subway.analyticsservice.service.RedisHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class RedisManagementController {

    private final RedisHealthService redisHealthService;

    /**
     * Redis 연결 상태 확인
     * GET /api/cache/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> checkRedisHealth() {
        log.info("Request: Check Redis health");

        boolean isAvailable = redisHealthService.isRedisAvailable();

        if (isAvailable) {
            return ResponseEntity.ok(ApiResponse.success("Redis is available", "OK"));
        } else {
            return ResponseEntity.ok(ApiResponse.error("Redis is not available"));
        }
    }

    /**
     * 특정 패턴의 캐시 클리어
     * DELETE /api/cache/clear?pattern=congestion
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clearCache(
            @RequestParam(required = false, defaultValue = "*") String pattern) {
        log.info("Request: Clear cache with pattern - {}", pattern);

        try {
            redisHealthService.clearCache(pattern);
            return ResponseEntity.ok(ApiResponse.success(
                    "Cache cleared successfully for pattern: " + pattern, "OK"));
        } catch (Exception e) {
            log.error("Failed to clear cache: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to clear cache: " + e.getMessage()));
        }
    }

    /**
     * 전체 캐시 클리어
     * DELETE /api/cache/clear-all
     */
    @DeleteMapping("/clear-all")
    public ResponseEntity<ApiResponse<String>> clearAllCache() {
        log.info("Request: Clear all cache");

        try {
            redisHealthService.clearAllCache();
            return ResponseEntity.ok(ApiResponse.success("All cache cleared successfully", "OK"));
        } catch (Exception e) {
            log.error("Failed to clear all cache: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to clear all cache: " + e.getMessage()));
        }
    }
}