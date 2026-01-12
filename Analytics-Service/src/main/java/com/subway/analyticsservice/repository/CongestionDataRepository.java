package com.subway.analyticsservice.repository;

import com.subway.analyticsservice.entity.CongestionData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CongestionDataRepository extends JpaRepository<CongestionData, Long> {

    // 실시간 조회 (최신 1건)
    CongestionData findTopByStationNameAndLineNumberOrderByTimestampDesc(
            String stationName, String lineNumber);

    // 시간대별 통계
    List<CongestionData> findByStationNameAndTimestampAfterOrderByTimestampAsc(
            String stationName, LocalDateTime timestamp);

    // TOP 혼잡역 조회
    List<CongestionData> findByTimestampAfter(LocalDateTime timestamp);

    CongestionData findTopByStationNameOrderByTimestampDesc(String stationName);

    List<CongestionData> findByStationNameAndLineNumberAndTimestampAfterOrderByTimestampAsc(
            String stationName, String lineNumber, LocalDateTime timestamp);

    // 고유한 역 + 호선 조합 조회 (Elasticsearch 인덱싱용) - 추가!
    @Query("SELECT DISTINCT c.stationName, c.lineNumber FROM CongestionData c")
    List<Object[]> findDistinctStationAndLine();
}