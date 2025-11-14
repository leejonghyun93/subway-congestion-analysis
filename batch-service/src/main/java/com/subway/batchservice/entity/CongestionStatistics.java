package com.subway.batchservice.entity;

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

    private String lineNumber;
    private String stationName;
    private Integer hourSlot;
    private Double avgCongestion;
    private LocalDateTime timestamp;
}