package com.subway.predictionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelMetrics {

    private String modelVersion;
    private Double rmse;  // Root Mean Squared Error
    private Double mae;   // Mean Absolute Error
    private Double r2Score;  // R² Score
    private Integer trainingDataSize;
    private LocalDateTime trainedAt;
    private LocalDateTime lastUsedAt;
}