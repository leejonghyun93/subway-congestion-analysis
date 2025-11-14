package com.subway.batchservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_statistics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate statisticsDate;  // 통계 날짜

    @Column(nullable = false)
    private String lineNumber;  // 호선 번호

    @Column(nullable = false)
    private String stationName;  // 역 이름

    private Double avgCongestion;  // 평균 혼잡도
    private Double maxCongestion;  // 최대 혼잡도
    private Double minCongestion;  // 최소 혼잡도

    private Integer totalRecords;  // 총 데이터 건수

    private Integer peakHour;  // 최고 혼잡 시간대
    private Integer lowHour;   // 최저 혼잡 시간대

    @Column(nullable = false)
    private LocalDateTime createdAt;  // 생성 시간

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}