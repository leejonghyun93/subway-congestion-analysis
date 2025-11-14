package com.subway.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subway.notificationservice.dto.CongestionAlert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.congestion-alerts}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeCongestionAlert(String message) {
        log.info("Received Kafka message: {}", message);

        try {
            CongestionAlert alert = objectMapper.readValue(message, CongestionAlert.class);

            log.info("Processing congestion alert: line={}, station={}, congestion={}",
                    alert.getLineNumber(), alert.getStationName(), alert.getCongestion());

            // 알림 처리
            notificationService.processCongestionAlert(alert);

        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", e.getMessage(), e);
        }
    }
}