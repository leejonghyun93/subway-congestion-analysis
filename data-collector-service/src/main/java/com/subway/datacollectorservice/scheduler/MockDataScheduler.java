package com.subway.datacollectorservice.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
@ConditionalOnProperty(name = "subway.data.source", havingValue = "mock", matchIfMissing = true)
public class MockDataScheduler {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    // CSV에서 로딩한 역 목록 (동적으로 생성)
    private List<String> stations = new ArrayList<>();

    // 실제 데이터 기반 평균 혼잡도 저장
    private Map<String, Map<Integer, Double>> stationHourlyAvg = new HashMap<>();

    public MockDataScheduler(KafkaTemplate<String, String> kafkaTemplate,
                             ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        log.info("=== MockDataScheduler initialized ===");
    }

    @PostConstruct
    public void loadHistoricalData() {
        log.info("실제 혼잡도 데이터 로딩 중...");

        try {
            ClassPathResource resource = new ClassPathResource("data/congestion_data.csv");
            CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream(), "UTF-8"));

            List<String[]> rows = reader.readAll();

            log.info("CSV 전체 행 수: {}", rows.size());

            if (rows.isEmpty()) {
                log.warn("CSV 파일이 비어있습니다");
                return;
            }

            String[] header = rows.get(0);
            log.info("CSV 헤더 개수: {}", header.length);

            Set<String> uniqueStations = new HashSet<>();

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);

                if (row.length < 10) {
                    continue;
                }

                try {
                    String stationName = row[4];
                    uniqueStations.add(stationName);

                    for (int timeIdx = 6; timeIdx < row.length && timeIdx < 50; timeIdx++) {
                        try {
                            String congestionStr = row[timeIdx].trim();

                            if (congestionStr.isEmpty() || congestionStr.equals("-")) {
                                continue;
                            }

                            double congestion = Double.parseDouble(congestionStr);

                            int hour = 5 + ((timeIdx - 6) / 2);
                            if (hour >= 24) hour -= 24;

                            stationHourlyAvg
                                    .computeIfAbsent(stationName, k -> new HashMap<>())
                                    .merge(hour, congestion, (old, newVal) -> (old + newVal) / 2);

                        } catch (NumberFormatException e) {
                            // 무시
                        }
                    }

                } catch (Exception e) {
                    log.warn("행 파싱 실패: {}", e.getMessage());
                }
            }

            // CSV에서 로딩한 역 목록을 stations에 저장
            stations = new ArrayList<>(uniqueStations);

            log.info("실제 데이터 로딩 완료: {}개 역", stationHourlyAvg.size());
            log.info("Mock 생성 대상 역: {}개", stations.size());

            // 처음 10개 역만 출력
            stations.stream().limit(10).forEach(station ->
                    log.info("  - {}", station)
            );
            log.info("  ... (총 {}개)", stations.size());

        } catch (Exception e) {
            log.error("실제 데이터 로딩 실패: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void generateMockCongestionData() {
        if (stations.isEmpty()) {
            log.warn("역 목록이 비어있습니다");
            return;
        }

        log.info("=== Mock 데이터 생성 ({}개 역) ===", stations.size());

        try {
            for (String station : stations) {
                Map<String, Object> data = createMockData(station);
                String jsonData = objectMapper.writeValueAsString(data);

                kafkaTemplate.send("congestion-data", jsonData);
            }

            log.info("Mock 데이터 생성 완료: {}건", stations.size());

        } catch (Exception e) {
            log.error("Mock 데이터 생성 실패: {}", e.getMessage(), e);
        }
    }

    private Map<String, Object> createMockData(String stationName) {
        int hour = LocalDateTime.now().getHour();

        double baseCongestion;
        if (stationHourlyAvg.containsKey(stationName) &&
                stationHourlyAvg.get(stationName).containsKey(hour)) {
            baseCongestion = stationHourlyAvg.get(stationName).get(hour);
        } else {
            baseCongestion = getBaseCongestionByTime(hour);
        }

        double congestionLevel = Math.min(95.0,
                Math.max(10.0, baseCongestion + (random.nextDouble() * 20 - 10)));

        int passengerCount = (int) (800 + (congestionLevel / 100.0) * 1500 + random.nextInt(300));

        Map<String, Object> data = new HashMap<>();
        data.put("stationName", stationName);
        data.put("lineNumber", "2");
        data.put("congestionLevel", Math.round(congestionLevel * 10) / 10.0);
        data.put("passengerCount", passengerCount);
        data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return data;
    }

    private double getBaseCongestionByTime(int hour) {
        if (hour >= 7 && hour <= 9) {
            return 85.0;
        } else if (hour >= 18 && hour <= 20) {
            return 90.0;
        } else if (hour >= 12 && hour <= 14) {
            return 70.0;
        } else if (hour >= 22 || hour <= 5) {
            return 40.0;
        } else {
            return 60.0;
        }
    }
}