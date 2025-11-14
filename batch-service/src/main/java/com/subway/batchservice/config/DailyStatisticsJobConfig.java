package com.subway.batchservice.config;

import com.subway.batchservice.entity.CongestionStatistics;
import com.subway.batchservice.entity.DailyStatistics;
import com.subway.batchservice.repository.CongestionStatisticsRepository;
import com.subway.batchservice.repository.DailyStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DailyStatisticsJobConfig {

    private final CongestionStatisticsRepository congestionStatisticsRepository;
    private final DailyStatisticsRepository dailyStatisticsRepository;

    @Bean
    public Job dailyStatisticsJob(JobRepository jobRepository, Step dailyStatisticsStep) {
        return new JobBuilder("dailyStatisticsJob", jobRepository)
                .start(dailyStatisticsStep)
                .build();
    }

    @Bean
    public Step dailyStatisticsStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager) {
        return new StepBuilder("dailyStatisticsStep", jobRepository)
                .<Map.Entry<String, List<CongestionStatistics>>, DailyStatistics>chunk(10, transactionManager)
                .reader(dailyStatisticsReader())
                .processor(dailyStatisticsProcessor())
                .writer(dailyStatisticsWriter())
                .build();
    }

    @Bean
    public ItemReader<Map.Entry<String, List<CongestionStatistics>>> dailyStatisticsReader() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = yesterday.atTime(23, 59, 59);

        log.info("Reading congestion data for date: {}", yesterday);

        List<CongestionStatistics> dataList = congestionStatisticsRepository
                .findByTimestampBetween(start, end);

        log.info("Found {} records for {}", dataList.size(), yesterday);

        // 역별로 그룹화
        Map<String, List<CongestionStatistics>> groupedData = dataList.stream()
                .collect(Collectors.groupingBy(
                        data -> data.getLineNumber() + "_" + data.getStationName()
                ));


        return new ListItemReader<>(new ArrayList<>(groupedData.entrySet()));
    }

    @Bean
    public ItemProcessor<Map.Entry<String, List<CongestionStatistics>>, DailyStatistics> dailyStatisticsProcessor() {
        return entry -> {
            String key = entry.getKey();
            List<CongestionStatistics> dataList = entry.getValue();

            if (dataList.isEmpty()) {
                return null;
            }

            String[] parts = key.split("_");
            String lineNumber = parts[0];
            String stationName = parts[1];

            // 통계 계산
            DoubleSummaryStatistics stats = dataList.stream()
                    .mapToDouble(CongestionStatistics::getAvgCongestion)
                    .summaryStatistics();

            // 최고/최저 혼잡 시간대 계산
            CongestionStatistics maxCongestionData = dataList.stream()
                    .max(Comparator.comparing(CongestionStatistics::getAvgCongestion))
                    .orElse(null);

            CongestionStatistics minCongestionData = dataList.stream()
                    .min(Comparator.comparing(CongestionStatistics::getAvgCongestion))
                    .orElse(null);

            return DailyStatistics.builder()
                    .statisticsDate(LocalDate.now().minusDays(1))
                    .lineNumber(lineNumber)
                    .stationName(stationName)
                    .avgCongestion(stats.getAverage())
                    .maxCongestion(stats.getMax())
                    .minCongestion(stats.getMin())
                    .totalRecords((int) stats.getCount())
                    .peakHour(maxCongestionData != null ? maxCongestionData.getHourSlot() : null)
                    .lowHour(minCongestionData != null ? minCongestionData.getHourSlot() : null)
                    .build();
        };
    }

    @Bean
    public ItemWriter<DailyStatistics> dailyStatisticsWriter() {
        return items -> {
            log.info("Writing {} daily statistics records", items.size());
            dailyStatisticsRepository.saveAll(items);
            log.info("Daily statistics saved successfully");
        };
    }
}
