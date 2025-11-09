package com.subway.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CongestionResponse implements Serializable {

    private String lineNumber;
    private String stationName;
    private Integer hourSlot;
    private Double avgCongestion;
    private Double maxCongestion;
    private Double minCongestion;
    private Long dataCount;
    private String congestionLevel;  // "여유", "보통", "혼잡", "매우혼잡"

    // 혼잡도 레벨 자동 계산
    public String getCongestionLevel() {
        if (avgCongestion == null) return "데이터 없음";
        if (avgCongestion < 30) return "여유";
        if (avgCongestion < 60) return "보통";
        if (avgCongestion < 80) return "혼잡";
        return "매우혼잡";
    }
}
