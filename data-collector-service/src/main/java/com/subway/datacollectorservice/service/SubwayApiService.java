package com.subway.datacollectorservice.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@ConditionalOnProperty(name = "subway.data.source", havingValue = "real")
public class SubwayApiService {

    @Value("${seoul.api.key}")
    private String apiKey;

    @Value("${seoul.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AtomicInteger dailyCallCount = new AtomicInteger(0);

    private static final int DAILY_LIMIT = 800;
    private static final List<String> TARGET_LINES = Arrays.asList("2호선", "3호선", "4호선");

    public SubwayApiService(RestTemplate restTemplate,
                            KafkaTemplate<String, String> kafkaTemplate,
                            ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        log.info("=== SubwayApiService Constructor Called ===");
    }

    @PostConstruct
    public void init() {
        log.info("=== SubwayApiService initialized with REAL API ===");
        if (apiKey != null && !apiKey.isEmpty()) {
            log.info("API Key: {}***", apiKey.substring(0, Math.min(5, apiKey.length())));
        } else {
            log.warn("API Key is not set!");
        }
    }

    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void collectRealData() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();

        boolean isRushHour = (hour >= 7 && hour <= 9) || (hour >= 18 && hour <= 20);
        boolean isDaytime = (hour >= 10 && hour <= 17) && (minute % 5 == 0);
        boolean isNight = ((hour >= 21 || hour <= 6) && (minute % 10 == 0));

        if (!(isRushHour || isDaytime || isNight)) {
            log.debug("API 호출 스킵 (시간 조건 불일치)");
            return;
        }

        if (dailyCallCount.get() >= DAILY_LIMIT) {
            log.warn("API 호출 제한 도달: {}/{}", dailyCallCount.get(), DAILY_LIMIT);
            return;
        }

        log.info("=== Real API 데이터 수집 시작 ===");

        for (String line : TARGET_LINES) {
            try {
                SeoulApiResponse response = callSeoulApi(line);

                if (response != null && response.getRealtimePositionList() != null) {
                    log.info("API 응답 ({}): {}건", line, response.getRealtimePositionList().size());
                    processAndSendData(response.getRealtimePositionList(), line);
                } else {
                    log.warn("API 응답 없음: {}", line);
                }

                Thread.sleep(1000);

            } catch (Exception e) {
                log.error("API 호출 실패 ({}): {}", line, e.getMessage());
            }
        }
    }

    private SeoulApiResponse callSeoulApi(String line) {
        String url = String.format("%s/%s/json/realtimePosition/0/100/%s",
                apiUrl, apiKey, line);

        log.info("API 호출: {}", line);
        log.info("전체 URL: {}", url);  // 이 줄 추가!
        dailyCallCount.incrementAndGet();

        try {
            SeoulApiResponse response = restTemplate.getForObject(url, SeoulApiResponse.class);
            log.info("응답 받음: {}", response);  // 이 줄 추가!
            return response;
        } catch (Exception e) {
            log.error("API 호출 예외: {}", e.getMessage(), e);  // 이 줄 추가!
            return null;
        }
    }

    private void processAndSendData(List<RealtimePosition> positions, String line) {
        for (RealtimePosition pos : positions) {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("stationName", pos.getStationName());
                data.put("lineNumber", extractLineNumber(line));
                data.put("congestionLevel", estimateCongestion());
                data.put("passengerCount", (int)(estimateCongestion() * 20));
                data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                String jsonData = objectMapper.writeValueAsString(data);
                kafkaTemplate.send("congestion-data", jsonData);

            } catch (Exception e) {
                log.error("데이터 처리 실패: {}", e.getMessage());
            }
        }

        log.info("Kafka 전송 완료 ({}): {}건", line, positions.size());
    }

    private String extractLineNumber(String line) {
        return line.replaceAll("[^0-9]", "");
    }

    private double estimateCongestion() {
        int hour = LocalTime.now().getHour();
        Random random = new Random();

        if ((hour >= 7 && hour <= 9) || (hour >= 18 && hour <= 20)) {
            return 70 + (random.nextDouble() * 25);
        } else if (hour >= 10 && hour <= 17) {
            return 30 + (random.nextDouble() * 30);
        } else {
            return 10 + (random.nextDouble() * 20);
        }
    }

    public String getSubwayData() {
        log.info("Manual API call triggered");
        collectRealData();
        return "Data collection initiated";
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SeoulApiResponse {
        @JsonProperty("realtimePositionList")
        private List<RealtimePosition> realtimePositionList;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RealtimePosition {
        @JsonProperty("statnNm")
        private String stationName;

        @JsonProperty("trainLineNm")
        private String lineNumber;

        @JsonProperty("updnLine")
        private String direction;

        @JsonProperty("trainSttus")
        private String trainStatus;
    }
}