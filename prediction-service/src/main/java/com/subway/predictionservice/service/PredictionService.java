package com.subway.predictionservice.service;

import com.subway.predictionservice.dto.ModelMetrics;
import com.subway.predictionservice.dto.PredictionRequest;
import com.subway.predictionservice.dto.PredictionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final MachineLearningService mlService;

    /**
     * 혼잡도 예측 (캐싱 적용)
     */
    @Cacheable(value = "predictions",
            key = "#request.lineNumber + '_' + #request.stationName + '_' + #request.hourSlot")
    public PredictionResponse predictCongestion(PredictionRequest request) {
        log.info("Predicting congestion for: line={}, station={}, hour={}",
                request.getLineNumber(), request.getStationName(), request.getHourSlot());

        try {
            // ML 모델을 통한 예측
            Double predictedValue = mlService.predict(
                    request.getLineNumber(),
                    request.getStationName(),
                    request.getHourSlot(),
                    request.getDayOfWeek()
            );

            if (predictedValue == null) {
                log.warn("Prediction failed for request: {}", request);
                return null;
            }

            // 신뢰도 계산 (모델 성능 기반)
            Double confidence = calculateConfidence(predictedValue);

            return PredictionResponse.builder()
                    .lineNumber(request.getLineNumber())
                    .stationName(request.getStationName())
                    .hourSlot(request.getHourSlot())
                    .predictedCongestion(predictedValue)
                    .confidence(confidence)
                    .modelVersion(mlService.getCurrentModelVersion())
                    .predictedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error during prediction: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 여러 시간대 예측
     */
    public PredictionResponse[] predictMultipleHours(String lineNumber, String stationName,
                                                     int startHour, int endHour) {
        log.info("Predicting multiple hours for station: {}, hours: {}-{}",
                stationName, startHour, endHour);

        int numHours = endHour - startHour + 1;
        PredictionResponse[] predictions = new PredictionResponse[numHours];

        for (int i = 0; i < numHours; i++) {
            int hour = startHour + i;
            PredictionRequest request = PredictionRequest.builder()
                    .lineNumber(lineNumber)
                    .stationName(stationName)
                    .hourSlot(hour)
                    .build();

            predictions[i] = predictCongestion(request);
        }

        return predictions;
    }

    /**
     * 모델 재학습
     */
    public ModelMetrics retrainModel() {
        log.info("Starting model retraining...");
        return mlService.trainNewModel();
    }

    /**
     * 현재 모델 메트릭 조회
     */
    public ModelMetrics getModelMetrics() {
        return mlService.getCurrentMetrics();
    }

    /**
     * 신뢰도 계산 (간단한 방식)
     */
    private Double calculateConfidence(Double predictedValue) {
        ModelMetrics metrics = mlService.getCurrentMetrics();

        if (metrics == null || metrics.getR2Score() == null) {
            return 0.5;  // 기본값
        }

        // R² 스코어를 신뢰도로 변환 (0.0 ~ 1.0)
        return Math.max(0.0, Math.min(1.0, metrics.getR2Score()));
    }
}