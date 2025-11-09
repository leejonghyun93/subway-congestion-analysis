package com.subway.dataprocessorservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "processed_subway_data", indexes = {
        @Index(name = "idx_station_time", columnList = "station_name,record_time"),
        @Index(name = "idx_line", columnList = "line_name")
})
public class ProcessedData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_name", nullable = false, length = 50)
    private String stationName;

    @Column(name = "line_name", nullable = false, length = 20)
    private String lineName;

    @Column(name = "train_no", length = 20)
    private String trainNo;

    @Column(name = "direction", length = 20)
    private String direction;

    @Column(name = "congestion_level")
    private Integer congestionLevel;

    @Column(name = "record_time", nullable = false)
    private LocalDateTime recordTime;

    @Column(name = "processed_time", nullable = false)
    private LocalDateTime processedTime;

    @Column(name = "hour_of_day")
    private Integer hourOfDay;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "is_rush_hour")
    private Boolean isRushHour;
}