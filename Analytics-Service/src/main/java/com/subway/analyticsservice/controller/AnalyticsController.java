package com.subway.analyticsservice.controller;

import com.subway.analyticsservice.dto.ApiResponse;
import com.subway.analyticsservice.dto.CongestionResponse;
import com.subway.analyticsservice.entity.CongestionData;
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

    @GetMapping("/realtime/{stationName}/message")
    public ApiResponse<String> getRealtimeCongestionMessage(
            @PathVariable String stationName,
            @RequestParam(required = false) String lineNumber) {

        ApiResponse<CongestionResponse> raw = getRealtimeCongestionData(stationName, lineNumber);
        CongestionResponse d = raw.getData();
        if (d == null) return ApiResponse.success("데이터 없음");

        double avg = d.getAvgCongestion();
        String level = avg > 80 ? "매우혼잡" : avg > 50 ? "혼잡" : "여유";

        String msg = String.format("[%s] %s (%s호선) 실시간 혼잡도 혼잡도: %.1f%% 승객 수: 약 %d명 상태: %s",
                level, d.getStationName(), d.getLineNumber(), avg, d.getPassengerCount(), level);

        return ApiResponse.success(msg);
    }

    @GetMapping("/realtime/{stationName}/data")
    public ApiResponse<CongestionResponse> getRealtimeCongestionData(
            @PathVariable String stationName,
            @RequestParam(required = false) String lineNumber) {

        CongestionData data = analyticsService.getRealtimeCongestion(stationName, lineNumber);
        if (data == null) return ApiResponse.success(null);

        double avg = data.getCongestionLevel() == null ? 0.0 : data.getCongestionLevel();

        CongestionResponse response = CongestionResponse.builder()
                .stationName(data.getStationName())
                .lineNumber(data.getLineNumber())
                .avgCongestion(avg)
                .passengerCount(data.getPassengerCount())
                .build();

        return ApiResponse.success(response);
    }



}