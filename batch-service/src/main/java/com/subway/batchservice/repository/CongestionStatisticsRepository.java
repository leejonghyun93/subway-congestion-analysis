package com.subway.batchservice.repository;

import com.subway.batchservice.entity.CongestionStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CongestionStatisticsRepository extends JpaRepository<CongestionStatistics, Long> {

    @Query("SELECT c FROM CongestionStatistics c WHERE c.timestamp BETWEEN :start AND :end")
    List<CongestionStatistics> findByTimestampBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}