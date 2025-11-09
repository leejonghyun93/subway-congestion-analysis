package com.subway.predictionservice.repository;

import com.subway.predictionservice.entity.CongestionStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CongestionStatisticsRepository extends JpaRepository<CongestionStatistics, Long> {

    // 특정 역의 모든 시간대 데이터 조회 (ML 학습용)
    List<CongestionStatistics> findByStationName(String stationName);

    // 특정 호선의 모든 데이터 조회 (ML 학습용)
    List<CongestionStatistics> findByLineNumber(String lineNumber);

    // 전체 데이터 조회 (ML 학습용)
    @Query("SELECT c FROM CongestionStatistics c ORDER BY c.lineNumber, c.stationName, c.hourSlot")
    List<CongestionStatistics> findAllForTraining();
}