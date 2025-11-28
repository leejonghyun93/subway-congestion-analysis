package com.subway.datacollectorservice.dto;

import lombok.Data;

@Data
public class CongestionHistoricalData {
    private String lineName;      // 호선명
    private String stationName;   // 역명
    private String timeSlot;      // 시간대 (05:30)
    private Integer congestion;   // 혼잡도
}