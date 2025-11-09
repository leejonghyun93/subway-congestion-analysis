package com.subway.dataprocessorservice.service;

import com.subway.dataprocessorservice.model.ProcessedData;
import com.subway.dataprocessorservice.model.SubwayRealtimeData;
import com.subway.dataprocessorservice.processor.SparkDataProcessor;
import com.subway.dataprocessorservice.repository.ProcessedDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataProcessingService {

    private final SparkDataProcessor sparkProcessor;
    private final ProcessedDataRepository repository;

    /**
     * 실시간 데이터 처리 및 저장
     */
    @Transactional
    public void processAndSave(SubwayRealtimeData rawData) {
        try {
            ProcessedData processed = sparkProcessor.processRealtimeData(rawData);
            repository.save(processed);
            log.debug("데이터 처리 완료: {}", processed.getStationName());
        } catch (Exception e) {
            log.error("데이터 처리 실패: {}", e.getMessage());
        }
    }

    /**
     * 배치 데이터 처리
     */
    @Transactional
    public void processBatch(List<SubwayRealtimeData> batchData) {
        try {
            List<ProcessedData> processedList = sparkProcessor.processBatch(batchData);
            repository.saveAll(processedList);
            log.info("배치 처리 완료: {} 건", processedList.size());
        } catch (Exception e) {
            log.error("배치 처리 실패: {}", e.getMessage());
        }
    }
}