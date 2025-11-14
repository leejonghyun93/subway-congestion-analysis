package com.subway.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CongestionAlert {
    private String lineNumber;
    private String stationName;
    private Double congestion;
    private String alertType;  // HIGH_CONGESTION, NORMAL, etc.
    private LocalDateTime timestamp;
}