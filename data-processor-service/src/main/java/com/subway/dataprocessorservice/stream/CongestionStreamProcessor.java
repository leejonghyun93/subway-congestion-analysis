package com.subway.dataprocessorservice.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subway.dataprocessorservice.entity.CongestionData;
import com.subway.dataprocessorservice.repository.CongestionDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Configuration
public class CongestionStreamProcessor {

    private static final String INPUT_TOPIC = "subway-congestion-data";
    private static final String OUTPUT_TOPIC = "processed-congestion-data";
    private static final String ALERT_TOPIC = "congestion-alerts";

    private final ObjectMapper objectMapper;
    private final CongestionDataRepository congestionDataRepository;

    public CongestionStreamProcessor(ObjectMapper objectMapper,
                                     CongestionDataRepository congestionDataRepository) {
        this.objectMapper = objectMapper;
        this.congestionDataRepository = congestionDataRepository;
    }

    @Bean
    public KStream<String, String> processStream(StreamsBuilder streamsBuilder) {

        KStream<String, String> inputStream = streamsBuilder
                .stream(INPUT_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));

        // 1. 데이터 처리 및 저장
        inputStream.foreach((key, value) -> {
            try {
                Map<String, Object> data = objectMapper.readValue(value, Map.class);

                CongestionData entity = CongestionData.builder()
                        .stationName(String.valueOf(data.get("stationName")))
                        .lineNumber(String.valueOf(data.get("lineNumber")))
                        .congestionLevel(Double.parseDouble(String.valueOf(data.get("congestionLevel"))))
                        .passengerCount(data.get("passengerCount") != null ?
                                Integer.parseInt(String.valueOf(data.get("passengerCount"))) : 0)
                        .timestamp(LocalDateTime.now())
                        .build();

                congestionDataRepository.save(entity);

                log.info("Kafka Streams 처리: {} - {}% ({})",
                        entity.getStationName(),
                        entity.getCongestionLevel(),
                        getCongestionStatus(entity.getCongestionLevel()));

            } catch (Exception e) {
                log.error("Stream 처리 실패: {}", e.getMessage());
            }
        });

        // 2. 처리된 데이터를 Output Topic으로 전송
        inputStream.to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        // 3. 혼잡도 80% 이상인 경우 Alert Topic으로 전송
        inputStream
                .filter((key, value) -> {
                    try {
                        Map<String, Object> data = objectMapper.readValue(value, Map.class);
                        double level = Double.parseDouble(String.valueOf(data.get("congestionLevel")));
                        return level >= 80.0;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .to(ALERT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        return inputStream;
    }

    private String getCongestionStatus(Double level) {
        if (level < 40) return "LOW";
        if (level < 60) return "MEDIUM";
        if (level < 80) return "HIGH";
        return "VERY_HIGH";
    }
}