package com.subway.datacollectorservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subway.datacollectorservice.model.SubwayRealtimeData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubwayApiService {

    @Value("${subway.api.key}")
    private String apiKey;

    @Value("${subway.api.base-url}")
    private String baseUrl;

    private final WebClient webClient = WebClient.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<SubwayRealtimeData> fetchRealtimeData(String stationName) {
        try {
            String url = String.format("%s/%s/json/realtimeStationArrival/0/10/%s",
                    baseUrl, apiKey, stationName);

            log.debug("API 호출: {}", url);

            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseResponse(response, stationName);

        } catch (Exception e) {
            log.error("API 호출 실패 - 역명: {}, 에러: {}", stationName, e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<SubwayRealtimeData> parseResponse(String jsonResponse, String stationName) {
        List<SubwayRealtimeData> dataList = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode realtimeArrivalList = root.path("realtimeArrivalList");

            if (realtimeArrivalList.isArray()) {
                for (JsonNode node : realtimeArrivalList) {
                    SubwayRealtimeData data = SubwayRealtimeData.builder()
                            .stationName(node.path("statnNm").asText())
                            .lineName(node.path("subwayId").asText())
                            .trainNo(node.path("btrainNo").asText())
                            .direction(node.path("updnLine").asText())
                            .trainStatus(node.path("trainLineNm").asText())
                            .previousStation(node.path("arvlMsg2").asText())
                            .nextStation(node.path("arvlMsg3").asText())
                            .arrivalTime(node.path("barvlDt").asText())
                            .congestionLevel(parseCongestionLevel(node.path("recptnDt").asText()))
                            .timestamp(LocalDateTime.now())
                            .rawData(node.toString())
                            .isExpress(node.path("bstatnNm").asText().contains("급행"))
                            .isLastTrain(node.path("bstatnNm").asText().contains("막차"))
                            .build();

                    dataList.add(data);
                }
            }

            log.info("{} 역 데이터 {} 건 파싱 완료", stationName, dataList.size());

        } catch (Exception e) {
            log.error("JSON 파싱 실패: {}", e.getMessage());
        }

        return dataList;
    }

    private Integer parseCongestionLevel(String rawValue) {
        int hour = LocalDateTime.now().getHour();

        if ((hour >= 7 && hour <= 9) || (hour >= 18 && hour <= 20)) {
            return 4;
        } else if (hour >= 10 && hour <= 17) {
            return 2;
        } else {
            return 1;
        }
    }
}