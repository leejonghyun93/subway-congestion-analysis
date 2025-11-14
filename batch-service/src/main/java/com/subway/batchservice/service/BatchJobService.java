package com.subway.batchservice.service;

import com.subway.batchservice.entity.BatchJobHistory;
import com.subway.batchservice.repository.BatchJobHistoryRepository;
import com.subway.batchservice.repository.DailyStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobService {

    private final JobLauncher jobLauncher;
    private final Job dailyStatisticsJob;
    private final BatchJobHistoryRepository batchJobHistoryRepository;
    private final DailyStatisticsRepository dailyStatisticsRepository;
    private final ReportGenerationService reportGenerationService;

    /**
     * 일일 통계 배치 실행
     */
    public JobExecution runDailyStatisticsJob() {
        LocalDateTime startTime = LocalDateTime.now();
        BatchJobHistory history = BatchJobHistory.builder()
                .jobName("dailyStatisticsJob")
                .jobType("DAILY")
                .status("RUNNING")
                .startTime(startTime)
                .build();
        batchJobHistoryRepository.save(history);

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("date", LocalDate.now().toString())
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(dailyStatisticsJob, jobParameters);

            // 이력 업데이트
            LocalDateTime endTime = LocalDateTime.now();
            history.setEndTime(endTime);
            history.setExecutionTimeMs(
                    java.time.Duration.between(startTime, endTime).toMillis());
            history.setStatus(execution.getStatus().toString());

            if (execution.getStatus() == BatchStatus.COMPLETED) {
                history.setProcessedRecords((int) execution.getStepExecutions().stream()
                        .mapToLong(StepExecution::getWriteCount)
                        .sum());
            } else {
                history.setErrorMessage(execution.getAllFailureExceptions().toString());
            }

            batchJobHistoryRepository.save(history);

            return execution;

        } catch (Exception e) {
            log.error("Daily statistics job failed: {}", e.getMessage(), e);
            history.setStatus("FAILED");
            history.setErrorMessage(e.getMessage());
            history.setEndTime(LocalDateTime.now());
            batchJobHistoryRepository.save(history);
            throw new RuntimeException("Daily statistics job failed", e);
        }
    }

    /**
     * 주간 리포트 배치 실행
     */
    public void runWeeklyReportJob() {
        LocalDateTime startTime = LocalDateTime.now();
        BatchJobHistory history = BatchJobHistory.builder()
                .jobName("weeklyReportJob")
                .jobType("WEEKLY")
                .status("RUNNING")
                .startTime(startTime)
                .build();
        batchJobHistoryRepository.save(history);

        try {
            log.info("Generating weekly report...");

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(7);

            // 리포트 생성
            String reportPath = reportGenerationService.generateWeeklyReport(startDate, endDate);

            // 이력 업데이트
            LocalDateTime endTime = LocalDateTime.now();
            history.setEndTime(endTime);
            history.setExecutionTimeMs(
                    java.time.Duration.between(startTime, endTime).toMillis());
            history.setStatus("SUCCESS");
            history.setJobParameters("{\"reportPath\":\"" + reportPath + "\"}");
            batchJobHistoryRepository.save(history);

            log.info("Weekly report generated successfully: {}", reportPath);

        } catch (Exception e) {
            log.error("Weekly report job failed: {}", e.getMessage(), e);
            history.setStatus("FAILED");
            history.setErrorMessage(e.getMessage());
            history.setEndTime(LocalDateTime.now());
            batchJobHistoryRepository.save(history);
            throw new RuntimeException("Weekly report job failed", e);
        }
    }

    /**
     * 월간 데이터 정리 배치 실행
     */
    public void runMonthlyCleanupJob() {
        LocalDateTime startTime = LocalDateTime.now();
        BatchJobHistory history = BatchJobHistory.builder()
                .jobName("monthlyCleanupJob")
                .jobType("MONTHLY")
                .status("RUNNING")
                .startTime(startTime)
                .build();
        batchJobHistoryRepository.save(history);

        try {
            log.info("Cleaning up old data...");

            // 6개월 이전 데이터 삭제
            LocalDate cutoffDate = LocalDate.now().minusMonths(6);
            dailyStatisticsRepository.deleteByStatisticsDateBefore(cutoffDate);

            // 이력 업데이트
            LocalDateTime endTime = LocalDateTime.now();
            history.setEndTime(endTime);
            history.setExecutionTimeMs(
                    java.time.Duration.between(startTime, endTime).toMillis());
            history.setStatus("SUCCESS");
            batchJobHistoryRepository.save(history);

            log.info("Monthly cleanup completed successfully");

        } catch (Exception e) {
            log.error("Monthly cleanup job failed: {}", e.getMessage(), e);
            history.setStatus("FAILED");
            history.setErrorMessage(e.getMessage());
            history.setEndTime(LocalDateTime.now());
            batchJobHistoryRepository.save(history);
            throw new RuntimeException("Monthly cleanup job failed", e);
        }
    }
}