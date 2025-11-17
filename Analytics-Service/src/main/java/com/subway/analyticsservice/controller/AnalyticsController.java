package com.subway.analyticsservice.controller;

import com.subway.analyticsservice.dto.ApiResponse;
import com.subway.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/top-congested")
    public ApiResponse<?> getTopCongestedStations(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(analyticsService.getTopCongestedStations(limit));
    }

    @GetMapping("/hourly")
    public ApiResponse<?> getHourlyStatistics(
            @RequestParam String stationName,
            @RequestParam String lineNumber) {
        return ApiResponse.success(analyticsService.getHourlyStatistics(stationName, lineNumber));
    }

    @GetMapping("/realtime/{stationName}")
    public ApiResponse<?> getRealtimeCongestion(
            @PathVariable String stationName,
            @RequestParam String lineNumber) {
        return ApiResponse.success(analyticsService.getRealtimeCongestion(stationName, lineNumber));
    }
}