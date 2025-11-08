package com.subway.datacollectorservice.controller;

import com.subway.datacollectorservice.model.SubwayRealtimeData;
import com.subway.datacollectorservice.repository.RawDataRepository;
import com.subway.datacollectorservice.service.KafkaProducerService;
import com.subway.datacollectorservice.service.SubwayApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/collect")
@RequiredArgsConstructor
public class CollectorController {

    private final SubwayApiService subwayApiService;
    private final KafkaProducerService kafkaProducerService;
    private final RawDataRepository rawDataRepository;

    @Value("${subway.target-stations}")
    private List<String> targetStations;

    @Scheduled(fixedRate = 60000, initialDelay = 5000)
    public void collectDataScheduled() {
        log.info("========== 정기 데이터 수집 시작 ==========");

        int totalCollected = 0;
        for (String station : targetStations) {
            totalCollected += collectStationData(station);
        }

        log.info("========== 정기 데이터 수집 완료: 총 {} 건 ==========", totalCollected);
    }

    @PostMapping("/manual")
    public String collectManually(@RequestParam String stationName) {
        log.info("수동 수집 요청: {}", stationName);
        int count = collectStationData(stationName);
        return String.format("수집 완료: %s 역 - %d 건", stationName, count);
    }

    @GetMapping("/status")
    public String getStatus() {
        long totalRecords = rawDataRepository.count();
        return String.format("Data Collector Service 정상 작동 중\n수집된 데이터: %d 건", totalRecords);
    }

    private int collectStationData(String stationName) {
        try {
            List<SubwayRealtimeData> dataList = subwayApiService.fetchRealtimeData(stationName);

            if (dataList.isEmpty()) {
                log.warn("{} 역 - 수집된 데이터 없음", stationName);
                return 0;
            }

            List<SubwayRealtimeData> savedData = rawDataRepository.saveAll(dataList);
            kafkaProducerService.sendBatchToKafka(savedData);

            log.info("{} 역 - {} 건 수집 완료", stationName, savedData.size());
            return savedData.size();

        } catch (Exception e) {
            log.error("{} 역 수집 실패: {}", stationName, e.getMessage());
            return 0;
        }
    }
}