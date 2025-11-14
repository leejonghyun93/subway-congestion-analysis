package com.subway.batchservice.repository;

import com.subway.batchservice.entity.DailyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyStatisticsRepository extends JpaRepository<DailyStatistics, Long> {

    Optional<DailyStatistics> findByStatisticsDateAndLineNumberAndStationName(
            LocalDate date, String lineNumber, String stationName);

    List<DailyStatistics> findByStatisticsDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT d FROM DailyStatistics d WHERE d.statisticsDate = :date ORDER BY d.avgCongestion DESC")
    List<DailyStatistics> findTopCongestedByDate(@Param("date") LocalDate date);

    void deleteByStatisticsDateBefore(LocalDate date);
}