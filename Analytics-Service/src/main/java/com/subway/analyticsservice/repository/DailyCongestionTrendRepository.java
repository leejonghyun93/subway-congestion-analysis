package com.subway.analyticsservice.repository;

import com.subway.analyticsservice.entity.DailyCongestionTrend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyCongestionTrendRepository extends JpaRepository<DailyCongestionTrend, Long> {

    // 특정 역의 일별 트렌드 조회 (최근 N일)
    List<DailyCongestionTrend> findByStationNameAndDateBetweenOrderByDateDesc(
            String stationName, LocalDate startDate, LocalDate endDate);

    // 특정 호선의 일별 평균 혼잡도
    @Query("SELECT d FROM DailyCongestionTrend d " +
            "WHERE d.lineNumber = :lineNumber " +
            "AND d.date BETWEEN :startDate AND :endDate " +
            "ORDER BY d.date DESC")
    List<DailyCongestionTrend> findLineNumberTrend(
            @Param("lineNumber") String lineNumber,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}