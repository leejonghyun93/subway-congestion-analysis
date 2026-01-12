package com.subway.analyticsservice.search.service;

import com.subway.analyticsservice.repository.CongestionDataRepository;
import com.subway.analyticsservice.search.document.StationSearchDocument;
import com.subway.analyticsservice.search.repository.StationSearchRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StationSearchService {

    private final StationSearchRepository searchRepository;
    private final CongestionDataRepository congestionRepository;

    /**
     * 서비스 시작 시 PostgreSQL 데이터를 Elasticsearch에 인덱싱
     */
    @PostConstruct
    @Transactional(readOnly = true)
    public void indexAllStations() {
        log.info("Indexing stations to Elasticsearch...");

        try {
            // PostgreSQL에서 고유한 역 정보 가져오기
            List<Object[]> stations = congestionRepository
                    .findDistinctStationAndLine();

            if (stations.isEmpty()) {
                log.warn("No stations found in database!");
                return;
            }

            // Elasticsearch에 인덱싱
            List<StationSearchDocument> documents = stations.stream()
                    .map(station -> StationSearchDocument.builder()
                            .id(station[0] + "-" + station[1])  // "강남역-2"
                            .stationName((String) station[0])   // "강남역"
                            .lineNumber((String) station[1])    // "2"
                            .lineName(station[1] + "호선")       // "2호선"
                            .build())
                    .collect(Collectors.toList());

            searchRepository.saveAll(documents);

            log.info("Successfully indexed {} stations", documents.size());

        } catch (Exception e) {
            log.error("Failed to index stations", e);
        }
    }

    /**
     * 역 이름 검색
     */
    public List<StationSearchDocument> searchStations(String keyword) {
        log.info("Searching stations with keyword: {}", keyword);
        return searchRepository.findByStationNameContaining(keyword);
    }

    /**
     * 역 이름 + 호선 검색
     */
    public List<StationSearchDocument> searchStations(String keyword, String lineNumber) {
        log.info("Searching stations with keyword: {} and line: {}", keyword, lineNumber);
        return searchRepository.findByStationNameContainingAndLineNumber(keyword, lineNumber);
    }

    /**
     * 전체 역 목록 조회
     */
    public List<StationSearchDocument> getAllStations() {
        return (List<StationSearchDocument>) searchRepository.findAll();
    }

    /**
     * 인덱스 재생성 (수동 트리거용)
     */
    public void reindex() {
        log.info("Deleting all documents...");
        searchRepository.deleteAll();

        log.info("Re-indexing all stations...");
        indexAllStations();
    }
}