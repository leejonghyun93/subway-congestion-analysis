package com.subway.analyticsservice.search.repository;

import com.subway.analyticsservice.search.document.StationSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationSearchRepository
        extends ElasticsearchRepository<StationSearchDocument, String> {

    // 역 이름 검색 (부분 일치)
    List<StationSearchDocument> findByStationNameContaining(String keyword);

    // 역 이름 + 호선 검색
    List<StationSearchDocument> findByStationNameContainingAndLineNumber(
            String keyword, String lineNumber);

    // 역 이름 검색 (정확히 일치)
    List<StationSearchDocument> findByStationName(String stationName);
}