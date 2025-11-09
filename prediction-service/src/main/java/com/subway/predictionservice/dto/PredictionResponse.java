package com.subway.predictionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse implements Serializable {

    private String lineNumber;
    private String stationName;
    private Integer hourSlot;
    private Double predictedCongestion;
    private Double confidence;  // 예측 신뢰도 (0.0 ~ 1.0)
    private String congestionLevel;  // "여유", "보통", "혼잡", "매우혼잡"
    private String modelVersion;
    private LocalDateTime predictedAt;

    // 혼잡도 레벨 계산
    public String getCongestionLevel() {
        if (predictedCongestion == null) return "알 수 없음";
        if (predictedCongestion < 30) return "여유";
        if (predictedCongestion < 60) return "보통";
        if (predictedCongestion < 80) return "혼잡";
        return "매우혼잡";
    }
}