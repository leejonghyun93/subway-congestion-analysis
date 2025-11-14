package com.subway.datacollectorservice.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class MockDataScheduler {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    // 2호선 주요 역 목록
    private final List<String> stations = Arrays.asList(
            "강남역", "홍대입구역", "신림역", "잠실역", "구로디지털단지역",
            "신촌역", "서울대입구역", "역삼역", "선릉역", "삼성역",
            "교대역", "사당역", "방배역", "서초역", "낙성대역",
            "봉천역", "신대방역", "문래역", "영등포구청역", "당산역"
    );

    public MockDataScheduler(KafkaTemplate<String, String> kafkaTemplate,
                             ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // 1분마다 실행
    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void generateMockCongestionData() {
        log.info("=== Generating Mock Congestion Data ===");

        try {
            for (String station : stations) {
                Map<String, Object> data = createMockData(station);
                String jsonData = objectMapper.writeValueAsString(data);

                kafkaTemplate.send("congestion-data", jsonData);

                log.info("Sent mock data - Station: {}, Congestion: {}%, Passengers: {}",
                        station,
                        String.format("%.1f", data.get("congestionLevel")),
                        data.get("passengerCount"));
            }

            log.info("=== Mock Data Generation Completed ===");

        } catch (Exception e) {
            log.error("Failed to generate mock data: {}", e.getMessage(), e);
        }
    }

    private Map<String, Object> createMockData(String stationName) {
        int hour = LocalDateTime.now().getHour();

        // 시간대별 기본 혼잡도 설정
        double baseCongestion = getBaseCongestionByTime(hour);

        // 역별 가중치 적용
        double stationWeight = getStationWeight(stationName);

        // 최종 혼잡도 계산 (50-95%)
        double congestionLevel = Math.min(95.0,
                baseCongestion * stationWeight + (random.nextDouble() * 10 - 5));

        // 승객 수 계산 (혼잡도에 비례)
        int passengerCount = (int) (800 + (congestionLevel / 100.0) * 1500 + random.nextInt(300));

        Map<String, Object> data = new HashMap<>();
        data.put("stationName", stationName.replace("역", ""));
        data.put("lineNumber", "2");
        data.put("congestionLevel", Math.round(congestionLevel * 10) / 10.0);
        data.put("passengerCount", passengerCount);
        data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return data;
    }

    // 시간대별 기본 혼잡도
    private double getBaseCongestionByTime(int hour) {
        if (hour >= 7 && hour <= 9) {
            return 85.0; // 출근 시간
        } else if (hour >= 18 && hour <= 20) {
            return 90.0; // 퇴근 시간
        } else if (hour >= 12 && hour <= 14) {
            return 70.0; // 점심 시간
        } else if (hour >= 22 || hour <= 5) {
            return 40.0; // 심야 시간
        } else {
            return 60.0; // 평시
        }
    }

    // 역별 가중치 (주요역은 더 혼잡)
    private double getStationWeight(String stationName) {
        Map<String, Double> weights = Map.of(
                "강남역", 1.2,
                "잠실역", 1.15,
                "홍대입구역", 1.15,
                "신림역", 1.1,
                "구로디지털단지역", 1.1,
                "역삼역", 1.05,
                "선릉역", 1.05
        );

        return weights.getOrDefault(stationName, 1.0);
    }
}