package com.subway.predictionservice.controller;

import com.subway.predictionservice.dto.ApiResponse;
import com.subway.predictionservice.dto.ModelMetrics;
import com.subway.predictionservice.dto.PredictionRequest;
import com.subway.predictionservice.dto.PredictionResponse;
import com.subway.predictionservice.service.PredictionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@Slf4j
@RestController
@RequestMapping("/api/prediction")
@RequiredArgsConstructor
public class PredictionController {

    @Lazy
    private final PredictionService predictionService;

    /**
     * 단일 시간대 혼잡도 예측
     * POST /api/prediction/predict
     */
    @PostMapping("/predict")
    public ResponseEntity<ApiResponse<PredictionResponse>> predictCongestion(
            @Valid @RequestBody PredictionRequest request) {
        log.info("Request: Predict congestion - {}", request);

        PredictionResponse prediction = predictionService.predictCongestion(request);

        if (prediction == null) {
            return ResponseEntity.ok(ApiResponse.error("예측에 실패했습니다. 모델이 준비되지 않았거나 데이터가 부족합니다."));
        }

        return ResponseEntity.ok(ApiResponse.success("혼잡도 예측 완료", prediction));
    }

    /**
     * 특정 역의 여러 시간대 예측
     * GET /api/prediction/station/{stationName}/hours?lineNumber=2&startHour=7&endHour=9
     */
    @GetMapping("/station/{stationName}/hours")
    public ResponseEntity<ApiResponse<PredictionResponse[]>> predictMultipleHours(
            @PathVariable String stationName,
            @RequestParam String lineNumber,
            @RequestParam(defaultValue = "7") int startHour,
            @RequestParam(defaultValue = "9") int endHour) {
        log.info("Request: Predict multiple hours - station={}, line={}, hours={}-{}",
                stationName, lineNumber, startHour, endHour);

        if (startHour < 0 || endHour > 23 || startHour > endHour) {
            return ResponseEntity.ok(ApiResponse.error("시간 범위가 올바르지 않습니다 (0-23)."));
        }

        PredictionResponse[] predictions = predictionService.predictMultipleHours(
                lineNumber, stationName, startHour, endHour);

        return ResponseEntity.ok(ApiResponse.success("여러 시간대 예측 완료", predictions));
    }

    /**
     * 현재 시간 기준 예측
     * GET /api/prediction/now?lineNumber=2&stationName=강남역
     */
    @GetMapping("/now")
    public ResponseEntity<ApiResponse<PredictionResponse>> predictNow(
            @RequestParam String lineNumber,
            @RequestParam String stationName) {
        log.info("Request: Predict now - line={}, station={}", lineNumber, stationName);

        int currentHour = java.time.LocalDateTime.now().getHour();

        PredictionRequest request = PredictionRequest.builder()
                .lineNumber(lineNumber)
                .stationName(stationName)
                .hourSlot(currentHour)
                .build();

        PredictionResponse prediction = predictionService.predictCongestion(request);

        if (prediction == null) {
            return ResponseEntity.ok(ApiResponse.error("현재 시간 예측에 실패했습니다."));
        }

        return ResponseEntity.ok(ApiResponse.success("현재 시간 혼잡도 예측 완료", prediction));
    }

    /**
     * 모델 메트릭 조회
     * GET /api/prediction/model/metrics
     */
    @GetMapping("/model/metrics")
    public ResponseEntity<ApiResponse<ModelMetrics>> getModelMetrics() {
        log.info("Request: Get model metrics");

        ModelMetrics metrics = predictionService.getModelMetrics();

        if (metrics == null) {
            return ResponseEntity.ok(ApiResponse.error("모델 메트릭 정보가 없습니다."));
        }

        return ResponseEntity.ok(ApiResponse.success("모델 메트릭 조회 완료", metrics));
    }

    /**
     * 모델 재학습
     * POST /api/prediction/model/retrain
     */
    @PostMapping("/model/retrain")
    public ResponseEntity<ApiResponse<ModelMetrics>> retrainModel() {
        log.info("Request: Retrain model");

        try {
            ModelMetrics metrics = predictionService.retrainModel();

            if (metrics == null) {
                return ResponseEntity.ok(ApiResponse.error("모델 재학습에 실패했습니다."));
            }

            return ResponseEntity.ok(ApiResponse.success("모델 재학습 완료", metrics));
        } catch (Exception e) {
            log.error("Model retraining failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("모델 재학습 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 헬스 체크
     * GET /api/prediction/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        ModelMetrics metrics = predictionService.getModelMetrics();
        String status = metrics != null ?
                "OK - Model: " + metrics.getModelVersion() :
                "Model not ready";

        return ResponseEntity.ok(ApiResponse.success(status, "Prediction Service is running"));
    }
}