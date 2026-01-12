package com.subway.analyticsservice.search.controller;

import com.subway.analyticsservice.dto.ApiResponse;
import com.subway.analyticsservice.search.document.StationSearchDocument;
import com.subway.analyticsservice.search.service.StationSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class StationSearchController {

    private final StationSearchService searchService;

    /**
     * 역 검색 API
     *
     * @param q 검색 키워드 (예: "강남")
     * @param line 호선 필터 (optional, 예: "2")
     * @return 검색 결과 리스트
     *
     * 예시:
     * - GET /api/search/stations?q=강남
     * - GET /api/search/stations?q=강남&line=2
     */
    @GetMapping("/stations")
    public ResponseEntity<ApiResponse<List<StationSearchDocument>>> searchStations(
            @RequestParam String q,
            @RequestParam(required = false) String line) {

        log.info("Station search request - keyword: {}, line: {}", q, line);

        List<StationSearchDocument> results;

        if (line != null && !line.isEmpty()) {
            results = searchService.searchStations(q, line);
        } else {
            results = searchService.searchStations(q);
        }

        log.info("Found {} stations", results.size());
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * 전체 역 목록 조회
     */
    @GetMapping("/stations/all")
    public ResponseEntity<ApiResponse<List<StationSearchDocument>>> getAllStations() {
        log.info("Get all stations request");
        List<StationSearchDocument> stations = searchService.getAllStations();
        return ResponseEntity.ok(ApiResponse.success(stations));
    }

    /**
     * 인덱스 재생성 (관리자용)
     */
    @PostMapping("/reindex")
    public ResponseEntity<ApiResponse<String>> reindex() {
        log.info("Re-indexing request received");
        searchService.reindex();
        return ResponseEntity.ok(ApiResponse.success("Re-indexing completed"));
    }
}