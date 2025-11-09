package com.subway.analyticsservice.controller;

import com.subway.analyticsservice.dto.ApiResponse;
import com.subway.analyticsservice.dto.CongestionResponse;
import com.subway.analyticsservice.dto.TrendResponse;
import com.subway.analyticsservice.service.CongestionAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class CongestionAnalyticsController {

    private final CongestionAnalyticsService analyticsService;

    /**
     * 특정 호선의 실시간 혼잡도 조회
     * GET /api/analytics/line/{lineNumber}
     */
    @GetMapping("/line/{lineNumber}")
    public ResponseEntity<ApiResponse<List<CongestionResponse>>> getCongestionByLine(
            @PathVariable String lineNumber) {
        log.info("Request: Get congestion by line - {}", lineNumber);

        List<CongestionResponse> data = analyticsService.getCongestionByLine(lineNumber);

        if (data.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("해당 호선의 데이터가 없습니다."));
        }

        return ResponseEntity.ok(ApiResponse.success(
                lineNumber + "호선 혼잡도 조회 성공", data));
    }

    /**
     * 특정 역의 시간대별 혼잡도 조회
     * GET /api/analytics/station/{stationName}
     */
    @GetMapping("/station/{stationName}")
    public ResponseEntity<ApiResponse<List<CongestionResponse>>> getCongestionByStation(
            @PathVariable String stationName) {
        log.info("Request: Get congestion by station - {}", stationName);

        List<CongestionResponse> data = analyticsService.getCongestionByStation(stationName);

        if (data.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("해당 역의 데이터가 없습니다."));
        }

        return ResponseEntity.ok(ApiResponse.success(
                stationName + " 혼잡도 조회 성공", data));
    }

    /**
     * 특정 역 + 특정 시간대 혼잡도 조회
     * GET /api/analytics/station/{stationName}/hour/{hourSlot}
     */
    @GetMapping("/station/{stationName}/hour/{hourSlot}")
    public ResponseEntity<ApiResponse<CongestionResponse>> getCongestionByStationAndHour(
            @PathVariable String stationName,
            @PathVariable Integer hourSlot) {
        log.info("Request: Get congestion by station and hour - {} at {}", stationName, hourSlot);

        CongestionResponse data = analyticsService.getCongestionByStationAndHour(stationName, hourSlot);

        if (data == null) {
            return ResponseEntity.ok(ApiResponse.error("해당 시간대의 데이터가 없습니다."));
        }

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 특정 호선 + 특정 시간대 혼잡도 조회
     * GET /api/analytics/line/{lineNumber}/hour/{hourSlot}
     */
    @GetMapping("/line/{lineNumber}/hour/{hourSlot}")
    public ResponseEntity<ApiResponse<List<CongestionResponse>>> getCongestionByLineAndHour(
            @PathVariable String lineNumber,
            @PathVariable Integer hourSlot) {
        log.info("Request: Get congestion by line and hour - {} at {}", lineNumber, hourSlot);

        List<CongestionResponse> data = analyticsService.getCongestionByLineAndHour(lineNumber, hourSlot);

        if (data.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("해당 시간대의 데이터가 없습니다."));
        }

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 혼잡한 역 TOP N 조회
     * GET /api/analytics/top-congestion?hourSlot=8&limit=10
     */
    @GetMapping("/top-congestion")
    public ResponseEntity<ApiResponse<List<CongestionResponse>>> getTopCongestionStations(
            @RequestParam(required = false, defaultValue = "8") Integer hourSlot,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        log.info("Request: Get top {} congested stations at hour {}", limit, hourSlot);

        List<CongestionResponse> data = analyticsService.getTopCongestionStations(hourSlot, limit);

        if (data.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("해당 시간대의 데이터가 없습니다."));
        }

        return ResponseEntity.ok(ApiResponse.success(
                "혼잡한 역 TOP " + limit + " 조회 성공", data));
    }

    /**
     * 특정 역의 일별 트렌드 조회 (최근 N일)
     * GET /api/analytics/trend/station/{stationName}?days=7
     */
    @GetMapping("/trend/station/{stationName}")
    public ResponseEntity<ApiResponse<List<TrendResponse>>> getDailyTrendByStation(
            @PathVariable String stationName,
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        log.info("Request: Get daily trend for station - {} (last {} days)", stationName, days);

        List<TrendResponse> data = analyticsService.getDailyTrend(stationName, days);

        if (data.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("해당 역의 트렌드 데이터가 없습니다."));
        }

        return ResponseEntity.ok(ApiResponse.success(
                stationName + " 일별 트렌드 조회 성공", data));
    }

    /**
     * 특정 호선의 일별 트렌드 조회 (최근 N일)
     * GET /api/analytics/trend/line/{lineNumber}?days=7
     */
    @GetMapping("/trend/line/{lineNumber}")
    public ResponseEntity<ApiResponse<List<TrendResponse>>> getDailyTrendByLine(
            @PathVariable String lineNumber,
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        log.info("Request: Get daily trend for line - {} (last {} days)", lineNumber, days);

        List<TrendResponse> data = analyticsService.getLineTrend(lineNumber, days);

        if (data.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("해당 호선의 트렌드 데이터가 없습니다."));
        }

        return ResponseEntity.ok(ApiResponse.success(
                lineNumber + "호선 일별 트렌드 조회 성공", data));
    }

    /**
     * 헬스 체크
     * GET /api/analytics/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        long count = analyticsService.getTotalStatisticsCount();
        return ResponseEntity.ok(ApiResponse.success(
                "Analytics Service is running. Total statistics: " + count, "OK"));
    }
}