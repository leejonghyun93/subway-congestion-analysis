package com.subway.chatbotservice.client;

import com.subway.chatbotservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "analytics-service", url = "${services.analytics.url}")
public interface AnalyticsClient {

    @GetMapping("/api/analytics/realtime/{stationName}/data")
    ApiResponse<Object> getRealtimeCongestion(
            @PathVariable String stationName,
            @RequestParam(required = false) String lineNumber
    );

    @GetMapping("/api/analytics/statistics/hourly")
    ApiResponse<Object> getHourlyStatistics(
            @RequestParam String lineNumber,
            @RequestParam String stationName
    );

    @GetMapping("/api/analytics/statistics/top-congested")
    ApiResponse<Object> getTopCongestedStations(
            @RequestParam(defaultValue = "10") int limit
    );
}
