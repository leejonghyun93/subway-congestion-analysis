package com.subway.chatbotservice.service;

import com.subway.chatbotservice.client.AnalyticsClient;
import com.subway.chatbotservice.client.PredictionClient;
import com.subway.chatbotservice.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataFetchService {

    private final AnalyticsClient analyticsClient;
    private final PredictionClient predictionClient;

    public Object fetchRealtimeCongestion(String lineNumber, String stationName) {
        try {
            ApiResponse<Object> response = analyticsClient.getRealtimeCongestion(stationName, lineNumber);
            return response.getData();
        } catch (Exception e) {
            log.error("Failed to fetch realtime congestion: {}", e.getMessage());
            return null;
        }
    }

    public Object fetchPrediction(String lineNumber, String stationName) {
        try {
            ApiResponse<Object> response = predictionClient.predictNow(lineNumber, stationName);
            return response.getData();
        } catch (Exception e) {
            log.error("Failed to fetch prediction: {}", e.getMessage());
            return null;
        }
    }

    public Object fetchStatistics(String lineNumber, String stationName) {
        try {
            ApiResponse<Object> response = analyticsClient.getHourlyStatistics(lineNumber, stationName);
            return response.getData();
        } catch (Exception e) {
            log.error("Failed to fetch statistics: {}", e.getMessage());
            return null;
        }
    }

    public Object fetchTopCongested(int limit) {
        try {
            ApiResponse<Object> response = analyticsClient.getTopCongestedStations(limit);
            return response.getData();
        } catch (Exception e) {
            log.error("Failed to fetch top congested: {}", e.getMessage());
            return null;
        }
    }
}