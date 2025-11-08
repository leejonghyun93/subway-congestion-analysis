package com.subway.datacollectorservice.service;

import com.subway.datacollectorservice.model.SubwayRealtimeData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private static final String TOPIC = "subway-realtime-data";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendToKafka(SubwayRealtimeData data) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(TOPIC, data.getStationName(), data);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Kafka 전송 성공: {}", data.getStationName());
                } else {
                    log.error("Kafka 전송 실패: {}", ex.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("Kafka 전송 오류: {}", e.getMessage());
        }
    }

    public void sendBatchToKafka(List<SubwayRealtimeData> dataList) {
        dataList.forEach(this::sendToKafka);
        log.info("Kafka 일괄 전송 완료: {} 건", dataList.size());
    }
}