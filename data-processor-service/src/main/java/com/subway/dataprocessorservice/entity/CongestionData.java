package com.subway.dataprocessorservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "congestion_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CongestionData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_name", nullable = false)
    private String stationName;

    @Column(name = "line_number", nullable = false)
    private String lineNumber;

    @Column(name = "congestion_level")
    private Double congestionLevel;

    @Column(name = "passenger_count")
    private Integer passengerCount;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}