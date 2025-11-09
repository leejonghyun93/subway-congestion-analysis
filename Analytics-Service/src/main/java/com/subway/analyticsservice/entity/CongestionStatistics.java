package com.subway.analyticsservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "congestion_statistics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CongestionStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "line_number", nullable = false, length = 10)
    private String lineNumber;

    @Column(name = "station_name", nullable = false, length = 100)
    private String stationName;

    @Column(name = "hour_slot", nullable = false)
    private Integer hourSlot;

    @Column(name = "avg_congestion")
    private Double avgCongestion;

    @Column(name = "max_congestion")
    private Double maxCongestion;

    @Column(name = "min_congestion")
    private Double minCongestion;

    @Column(name = "data_count")
    private Long dataCount;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}