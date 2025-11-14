package com.subway.batchservice.controller;

import com.subway.batchservice.dto.ApiResponse;
import com.subway.batchservice.entity.BatchJobHistory;
import com.subway.batchservice.entity.DailyStatistics;
import com.subway.batchservice.repository.BatchJobHistoryRepository;
import com.subway.batchservice.repository.DailyStatisticsRepository;
import com.subway.batchservice.service.BatchJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchJobService batchJobService;
    private final BatchJobHistoryRepository batchJobHistoryRepository;
    private final DailyStatisticsRepository dailyStatisticsRepository;

    /**
     * 일일 통계 배치 수동 실행
     * POST /api/batch/daily-statistics
     */
    @PostMapping("/daily-statistics")
    public ResponseEntity<ApiResponse<String>> runDailyStatistics() {
        log.info("Manual execution: Daily Statistics Job");

        try {
            JobExecution execution = batchJobService.runDailyStatisticsJob();
            String status = execution.getStatus().toString();

            return ResponseEntity.ok(ApiResponse.success(
                    "일일 통계 배치 실행 완료: " + status,
                    status));
        } catch (Exception e) {
            log.error("Daily statistics job failed: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("배치 실행 실패: " + e.getMessage()));
        }
    }

    /**
     * 주간 리포트 배치 수동 실행
     * POST /api/batch/weekly-report
     */
    @PostMapping("/weekly-report")
    public ResponseEntity<ApiResponse<String>> runWeeklyReport() {
        log.info("Manual execution: Weekly Report Job");

        try {
            batchJobService.runWeeklyReportJob();
            return ResponseEntity.ok(ApiResponse.success("주간 리포트 생성 완료", "SUCCESS"));
        } catch (Exception e) {
            log.error("Weekly report job failed: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("리포트 생성 실패: " + e.getMessage()));
        }
    }

    /**
     * 월간 정리 배치 수동 실행
     * POST /api/batch/monthly-cleanup
     */
    @PostMapping("/monthly-cleanup")
    public ResponseEntity<ApiResponse<String>> runMonthlyCleanup() {
        log.info("Manual execution: Monthly Cleanup Job");

        try {
            batchJobService.runMonthlyCleanupJob();
            return ResponseEntity.ok(ApiResponse.success("월간 데이터 정리 완료", "SUCCESS"));
        } catch (Exception e) {
            log.error("Monthly cleanup job failed: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("정리 실패: " + e.getMessage()));
        }
    }

    /**
     * 배치 실행 이력 조회
     * GET /api/batch/history
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<BatchJobHistory>>> getHistory(
            @RequestParam(required = false) String jobName) {
        log.info("Get batch job history: jobName={}", jobName);

        List<BatchJobHistory> history;
        if (jobName != null && !jobName.isEmpty()) {
            history = batchJobHistoryRepository.findByJobNameOrderByCreatedAtDesc(jobName);
        } else {
            history = batchJobHistoryRepository.findTop10ByOrderByCreatedAtDesc();
        }

        return ResponseEntity.ok(ApiResponse.success("배치 이력 조회 완료", history));
    }

    /**
     * 일별 통계 조회
     * GET /api/batch/statistics/daily
     */
    @GetMapping("/statistics/daily")
    public ResponseEntity<ApiResponse<List<DailyStatistics>>> getDailyStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Get daily statistics: {} to {}", startDate, endDate);

        List<DailyStatistics> statistics = dailyStatisticsRepository
                .findByStatisticsDateBetween(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success("일별 통계 조회 완료", statistics));
    }

    /**
     * 배치 통계 조회
     * GET /api/batch/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getStats() {
        log.info("Get batch statistics");

        long totalSuccess = batchJobHistoryRepository.countSuccessful();
        long totalFailed = batchJobHistoryRepository.countFailed();

        var stats = new Object() {
            public final long successful = totalSuccess;
            public final long failed = totalFailed;
            public final long total = totalSuccess + totalFailed;
        };

        return ResponseEntity.ok(ApiResponse.success("배치 통계 조회 완료", stats));
    }

    /**
     * 헬스 체크
     * GET /api/batch/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Batch Service is running", "OK"));
    }
}