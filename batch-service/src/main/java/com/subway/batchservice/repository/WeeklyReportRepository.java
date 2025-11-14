package com.subway.batchservice.repository;

import com.subway.batchservice.entity.WeeklyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {

    Optional<WeeklyReport> findByWeekStartDateAndWeekEndDate(LocalDate start, LocalDate end);

    List<WeeklyReport> findByWeekStartDateBetween(LocalDate start, LocalDate end);

    List<WeeklyReport> findTop10ByOrderByCreatedAtDesc();
}