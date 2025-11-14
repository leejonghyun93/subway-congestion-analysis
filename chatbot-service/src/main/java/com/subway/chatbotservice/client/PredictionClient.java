package com.subway.chatbotservice.client;

import com.subway.chatbotservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "prediction-service", url = "${services.prediction.url}")
public interface PredictionClient {

    @GetMapping("/api/prediction/now")
    ApiResponse<Object> predictNow(
            @RequestParam String lineNumber,
            @RequestParam String stationName
    );

    @GetMapping("/api/prediction/station/{stationName}/hours")
    ApiResponse<Object> predictMultipleHours(
            @RequestParam String lineNumber,
            @RequestParam String stationName,
            @RequestParam int startHour,
            @RequestParam int endHour
    );
}