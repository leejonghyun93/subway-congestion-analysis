package com.subway.datacollectorservice.controller;

import com.subway.datacollectorservice.service.SubwayApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/collector")
public class CollectorController {

    //  Optional로 변경
    private final Optional<SubwayApiService> subwayApiService;

    public CollectorController(@Autowired(required = false) SubwayApiService subwayApiService) {
        this.subwayApiService = Optional.ofNullable(subwayApiService);
        log.info("CollectorController initialized (SubwayApiService present: {})",
                this.subwayApiService.isPresent());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Data Collector Service is running");
    }

    @PostMapping("/collect")
    public ResponseEntity<String> collectData() {
        if (subwayApiService.isPresent()) {
            log.info("Using real API to collect data");
            subwayApiService.get().getSubwayData();
        } else {
            log.info("Using Mock data scheduler");
        }
        return ResponseEntity.ok("Data collection initiated");
    }
}