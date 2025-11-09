package com.subway.analyticsservice.repository;

import com.subway.analyticsservice.entity.CongestionStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CongestionStatisticsRepository extends JpaRepository<CongestionStatistics, Long> {

    // 특정 호선의 최신 통계 조회
    List<CongestionStatistics> findByLineNumberOrderByProcessedAtDesc(String lineNumber);

    // 특정 역의 시간대별 통계 조회
    List<CongestionStatistics> findByStationNameOrderByHourSlot(String stationName);

    // 특정 호선 + 시간대 통계 조회
    List<CongestionStatistics> findByLineNumberAndHourSlotOrderByAvgCongestionDesc(
            String lineNumber, Integer hourSlot);

    // 혼잡한 역 TOP N 조회
    @Query("SELECT c FROM CongestionStatistics c " +
            "WHERE c.hourSlot = :hourSlot " +
            "ORDER BY c.avgCongestion DESC")
    List<CongestionStatistics> findTopCongestionStations(
            @Param("hourSlot") Integer hourSlot);

    // 특정 역 + 시간대 조회
    CongestionStatistics findByStationNameAndHourSlot(String stationName, Integer hourSlot);
}
