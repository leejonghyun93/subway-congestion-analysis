package com.subway.dataprocessorservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subway.dataprocessorservice.entity.CongestionData;
import com.subway.dataprocessorservice.repository.CongestionDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CongestionDataConsumer {

    private final CongestionDataRepository congestionDataRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "congestion-data", groupId = "data-processor-group")
    public void consume(String message) {
        try {
            log.info("Received message: {}", message);

            // JSON String → Map
            Map<String, Object> data = objectMapper.readValue(message, Map.class);

            // Entity 생성
            CongestionData congestionData = CongestionData.builder()
                    .stationName(data.get("stationName").toString())
                    .lineNumber(data.get("lineNumber").toString())
                    .congestionLevel(Double.parseDouble(data.get("congestionLevel").toString()))
                    .passengerCount(Integer.parseInt(data.get("passengerCount").toString()))
                    .timestamp(LocalDateTime.parse(data.get("timestamp").toString()))
                    .build();

            // DB 저장
            congestionDataRepository.save(congestionData);

            log.info("Saved to DB: {} - {}% congestion",
                    congestionData.getStationName(),
                    congestionData.getCongestionLevel());

        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage(), e);
        }
    }
}