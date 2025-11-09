package com.subway.dataprocessorservice.repository;

import com.subway.dataprocessorservice.model.ProcessedData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProcessedDataRepository extends JpaRepository<ProcessedData, Long> {

    // 특정 역의 특정 시간대 데이터
    List<ProcessedData> findByStationNameAndRecordTimeBetween(
            String stationName,
            LocalDateTime start,
            LocalDateTime end
    );

    // 특정 호선의 평균 혼잡도
    @Query("SELECT AVG(p.congestionLevel) FROM ProcessedData p " +
            "WHERE p.lineName = :lineName " +
            "AND p.recordTime BETWEEN :start AND :end")
    Double getAverageCongestionByLine(
            @Param("lineName") String lineName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 출퇴근 시간 데이터
    List<ProcessedData> findByIsRushHourTrue();
}