package com.subway.predictionservice.repository;

import com.subway.predictionservice.entity.DailyCongestionTrend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyCongestionTrendRepository extends JpaRepository<DailyCongestionTrend, Long> {

    // 특정 역의 최근 N일 트렌드 조회
    List<DailyCongestionTrend> findByStationNameAndDateAfterOrderByDateDesc(
            String stationName, LocalDate afterDate);

    // 특정 호선의 최근 N일 트렌드 조회
    List<DailyCongestionTrend> findByLineNumberAndDateAfterOrderByDateDesc(
            String lineNumber, LocalDate afterDate);
}