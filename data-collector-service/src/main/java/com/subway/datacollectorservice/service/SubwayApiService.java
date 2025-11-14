package com.subway.datacollectorservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@ConditionalOnProperty(name = "mock.enabled", havingValue = "false", matchIfMissing = false)
// mock.enabled=false일 때만 활성화
public class SubwayApiService {

    @Value("${subway.api.key}")
    private String apiKey;

    @Value("${subway.api.url:http://openapi.seoul.go.kr:8088}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public SubwayApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        log.info("SubwayApiService initialized with real API");
    }

    public String getSubwayData() {
        log.info("Fetching real subway data from API");
        // 실제 API 호출 로직
        return null;
    }
}