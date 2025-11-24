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
            log.info("Calling Analytics API: station={}, line={}", stationName, lineNumber);

            ApiResponse<Object> response = analyticsClient.getRealtimeCongestion(stationName, lineNumber);

            log.info("Analytics API response: success={}, data={}",
                    response.isSuccess(), response.getData());

            // 데이터가 있으면 반환
            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            }

            // 데이터 없으면 null 반환 (Mock 생성 안 함!)
            log.warn("No data found for station={}, line={}", stationName, lineNumber);
            return null;

        } catch (Exception e) {
            log.error("Failed to fetch realtime congestion: {}", e.getMessage());
            return null;  // 에러 시에도 null 반환
        }
    }

    public Object fetchPrediction(String lineNumber, String stationName) {
        try {
            log.info("Calling Prediction API: station={}, line={}", stationName, lineNumber);

            ApiResponse<Object> response = predictionClient.predictNow(lineNumber, stationName);

            log.info("Prediction API response: success={}", response.isSuccess());

            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            }

            return null;

        } catch (Exception e) {
            log.error("Failed to fetch prediction: {}", e.getMessage());
            return null;
        }
    }

    public Object fetchStatistics(String lineNumber, String stationName) {
        try {
            log.info("Calling Statistics API: station={}, line={}", stationName, lineNumber);

            ApiResponse<Object> response = analyticsClient.getHourlyStatistics(lineNumber, stationName);

            log.info("Statistics API response: success={}", response.isSuccess());

            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            }

            return null;

        } catch (Exception e) {
            log.error("Failed to fetch statistics: {}", e.getMessage());
            return null;
        }
    }

    public Object fetchTopCongested(int limit) {
        try {
            log.info("Calling Top Congested API: limit={}", limit);

            ApiResponse<Object> response = analyticsClient.getTopCongestedStations(limit);

            log.info("Top Congested API response: success={}", response.isSuccess());

            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            }

            return null;

        } catch (Exception e) {
            log.error("Failed to fetch top congested: {}", e.getMessage());
            return null;
        }
    }
}