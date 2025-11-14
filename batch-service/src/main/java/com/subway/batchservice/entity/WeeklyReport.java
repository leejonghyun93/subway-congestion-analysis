package com.subway.batchservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "weekly_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate weekStartDate;  // 주 시작일

    @Column(nullable = false)
    private LocalDate weekEndDate;    // 주 종료일

    @Column(nullable = false)
    private Integer weekNumber;       // 주차

    @Column(columnDefinition = "TEXT")
    private String topCongestedStations;  // TOP 10 혼잡 역 (JSON)

    @Column(columnDefinition = "TEXT")
    private String hourlyAverages;        // 시간대별 평균 (JSON)

    private String reportFilePath;  // 리포트 파일 경로 (Excel)

    @Column(nullable = false)
    private String status;  // GENERATED, SENT, FAILED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}