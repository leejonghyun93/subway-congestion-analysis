package com.subway.predictionservice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_congestion_trend")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCongestionTrend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "line_number", nullable = false, length = 10)
    private String lineNumber;

    @Column(name = "station_name", nullable = false, length = 100)
    private String stationName;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "avg_congestion")
    private Double avgCongestion;

    @Column(name = "peak_hour")
    private Integer peakHour;

    @Column(name = "peak_congestion")
    private Double peakCongestion;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}