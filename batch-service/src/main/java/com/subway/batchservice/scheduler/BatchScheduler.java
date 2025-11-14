package com.subway.batchservice.scheduler;

import com.subway.batchservice.service.BatchJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final BatchJobService batchJobService;

    /**
     * 일일 통계 배치 (매일 새벽 1시)
     */
    @Scheduled(cron = "${batch.schedule.daily-statistics}")
    public void runDailyStatisticsJob() {
        log.info("====== Starting Daily Statistics Job ======");
        try {
            batchJobService.runDailyStatisticsJob();
            log.info("====== Daily Statistics Job Completed Successfully ======");
        } catch (Exception e) {
            log.error("====== Daily Statistics Job Failed: {} ======", e.getMessage(), e);
        }
    }

    /**
     * 주간 리포트 배치 (매주 월요일 새벽 2시)
     */
    @Scheduled(cron = "${batch.schedule.weekly-report}")
    public void runWeeklyReportJob() {
        log.info("====== Starting Weekly Report Job ======");
        try {
            batchJobService.runWeeklyReportJob();
            log.info("====== Weekly Report Job Completed Successfully ======");
        } catch (Exception e) {
            log.error("====== Weekly Report Job Failed: {} ======", e.getMessage(), e);
        }
    }

    /**
     * 월간 데이터 정리 배치 (매월 1일 새벽 3시)
     */
    @Scheduled(cron = "${batch.schedule.monthly-cleanup}")
    public void runMonthlyCleanupJob() {
        log.info("====== Starting Monthly Cleanup Job ======");
        try {
            batchJobService.runMonthlyCleanupJob();
            log.info("====== Monthly Cleanup Job Completed Successfully ======");
        } catch (Exception e) {
            log.error("====== Monthly Cleanup Job Failed: {} ======", e.getMessage(), e);
        }
    }
}