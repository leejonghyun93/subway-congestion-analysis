package com.subway.chatbotservice.service;

import com.subway.chatbotservice.client.AnalyticsClient;
import com.subway.chatbotservice.client.PredictionClient;
import com.subway.chatbotservice.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataFetchService {

    private final AnalyticsClient analyticsClient;
    private final PredictionClient predictionClient;

    public Object fetchRealtimeCongestion(String lineNumber, String stationName) {
        try {
            log.info("Calling Analytics API: station={}, line={}", stationName, lineNumber);

            ApiResponse<Object> response = analyticsClient.getRealtimeCongestion(stationName, lineNumber);

            log.info("Analytics API response: success={}, data={}",
                    response.isSuccess(), response.getData());

            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            } else {
                log.warn(" No data from Analytics API");
                return createMockData(stationName, lineNumber);  // Mock 데이터 대신 반환
            }
        } catch (Exception e) {
            log.error(" Failed to fetch realtime congestion: {}", e.getMessage(), e);
            return createMockData(stationName, lineNumber);  //  에러 시 Mock 데이터
        }
    }

    public Object fetchPrediction(String lineNumber, String stationName) {
        try {
            log.info("Calling Prediction API: station={}, line={}", stationName, lineNumber);

            ApiResponse<Object> response = predictionClient.predictNow(lineNumber, stationName);

            log.info("Prediction API response: success={}", response.isSuccess());

            return response.getData();
        } catch (Exception e) {
            log.error("Failed to fetch prediction: {}", e.getMessage(), e);
            return null;
        }
    }

    public Object fetchStatistics(String lineNumber, String stationName) {
        try {
            log.info("Calling Statistics API: station={}, line={}", stationName, lineNumber);

            ApiResponse<Object> response = analyticsClient.getHourlyStatistics(lineNumber, stationName);

            log.info("Statistics API response: success={}", response.isSuccess());

            return response.getData();
        } catch (Exception e) {
            log.error("Failed to fetch statistics: {}", e.getMessage(), e);
            return null;
        }
    }

    public Object fetchTopCongested(int limit) {
        try {
            log.info("Calling Top Congested API: limit={}", limit);

            ApiResponse<Object> response = analyticsClient.getTopCongestedStations(limit);

            log.info(" Top Congested API response: success={}", response.isSuccess());

            return response.getData();
        } catch (Exception e) {
            log.error(" Failed to fetch top congested: {}", e.getMessage(), e);
            return null;
        }
    }

    //  Mock 데이터 생성
    private Object createMockData(String stationName, String lineNumber) {
        log.info(" Creating mock data for: station={}, line={}", stationName, lineNumber);

        Map<String, Object> data = new HashMap<>();
        data.put("stationName", stationName);
        data.put("lineNumber", lineNumber);
        data.put("congestionLevel", 72.5);
        data.put("passengerCount", 1456);
        data.put("status", "보통");

        return data;
    }
}