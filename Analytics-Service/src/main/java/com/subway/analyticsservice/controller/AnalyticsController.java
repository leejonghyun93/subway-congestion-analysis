package com.subway.analyticsservice.controller;

import com.subway.analyticsservice.dto.ApiResponse;
import com.subway.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController  // 필수!
@RequestMapping("/api/analytics")  // 필수!
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // 실시간 혼잡도 조회
    @GetMapping("/realtime/{stationName}")
    public ResponseEntity<ApiResponse> getRealtimeCongestion(
            @PathVariable String stationName,
            @RequestParam(required = false) String lineNumber) {

        log.info("Request: realtime congestion - station: {}, line: {}", stationName, lineNumber);

        try {
            var data = analyticsService.getRealtimeCongestion(stationName, lineNumber);

            if (data == null) {
                log.warn("⚠️ No data found for station: {}", stationName);
                return ResponseEntity.ok(ApiResponse.success(null));
            }

            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            log.error("Error fetching realtime data: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("데이터 조회 실패: " + e.getMessage()));
        }
    }

    // 시간대별 통계 조회
    @GetMapping("/statistics/hourly")
    public ResponseEntity<ApiResponse> getHourlyStatistics(
            @RequestParam String stationName,
            @RequestParam(required = false) String lineNumber) {

        log.info("Request: hourly statistics - station: {}, line: {}", stationName, lineNumber);

        try {
            var data = analyticsService.getHourlyStatistics(stationName, lineNumber);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            log.error(" Error fetching hourly statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("통계 조회 실패: " + e.getMessage()));
        }
    }

    // 혼잡도 TOP 역 조회
    @GetMapping("/statistics/top-congested")
    public ResponseEntity<ApiResponse> getTopCongestedStations(
            @RequestParam(defaultValue = "10") int limit) {

        log.info(" Request: top congested stations - limit: {}", limit);

        try {
            var data = analyticsService.getTopCongestedStations(limit);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            log.error(" Error fetching top congested stations: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("순위 조회 실패: " + e.getMessage()));
        }
    }

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Analytics Service is running");
    }
}