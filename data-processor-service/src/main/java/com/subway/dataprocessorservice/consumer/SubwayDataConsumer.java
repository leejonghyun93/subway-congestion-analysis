package com.subway.dataprocessorservice.consumer;

import com.subway.dataprocessorservice.model.SubwayRealtimeData;
import com.subway.dataprocessorservice.service.DataProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubwayDataConsumer {

    private final DataProcessingService processingService;

    @KafkaListener(topics = "subway-realtime-data", groupId = "data-processor-group")
    public void consume(SubwayRealtimeData data) {
        log.info("Kafka 메시지 수신: {} - {}", data.getStationName(), data.getTimestamp());

        try {
            processingService.processAndSave(data);
        } catch (Exception e) {
            log.error("메시지 처리 실패: {}", e.getMessage(), e);
        }
    }
}