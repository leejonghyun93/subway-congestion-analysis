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
    private Double avgCongestion; // 숫자 그대로
    private Double maxCongestion;
    private Double minCongestion;
    private Long dataCount;
    private Integer passengerCount;
    private String congestionLevel; // null로 두고 getter 사용

    // React용 상태 문자열 계산
    public String getCongestionLevel() {
        if (avgCongestion == null) return "데이터 없음";
        if (avgCongestion < 30) return "여유";
        if (avgCongestion < 60) return "보통";
        if (avgCongestion < 80) return "혼잡";
        return "매우혼잡";
    }
}
