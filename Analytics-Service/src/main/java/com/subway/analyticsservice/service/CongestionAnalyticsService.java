package com.subway.analyticsservice.service;

import com.subway.analyticsservice.dto.CongestionResponse;
import com.subway.analyticsservice.dto.TrendResponse;
import com.subway.analyticsservice.entity.CongestionStatistics;
import com.subway.analyticsservice.entity.DailyCongestionTrend;
import com.subway.analyticsservice.repository.CongestionStatisticsRepository;
import com.subway.analyticsservice.repository.DailyCongestionTrendRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CongestionAnalyticsService {

    private final CongestionStatisticsRepository congestionStatisticsRepository;
    private final DailyCongestionTrendRepository dailyCongestionTrendRepository;

    /**
     * 특정 호선의 실시간 혼잡도 조회 (캐싱 적용)
     */
    @Cacheable(value = "congestion", key = "#lineNumber", unless = "#result == null || #result.isEmpty()")
    public List<CongestionResponse> getCongestionByLine(String lineNumber) {
        log.info("Fetching congestion data for line: {}", lineNumber);

        List<CongestionStatistics> statistics =
                congestionStatisticsRepository.findByLineNumberOrderByProcessedAtDesc(lineNumber);

        return statistics.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 특정 역의 시간대별 혼잡도 조회 (캐싱 적용)
     */
    @Cacheable(value = "congestion", key = "#stationName", unless = "#result == null || #result.isEmpty()")
    public List<CongestionResponse> getCongestionByStation(String stationName) {
        log.info("Fetching congestion data for station: {}", stationName);

        List<CongestionStatistics> statistics =
                congestionStatisticsRepository.findByStationNameOrderByHourSlot(stationName);

        return statistics.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 특정 역 + 시간대 혼잡도 조회 (캐싱 적용)
     */
    @Cacheable(value = "congestion", key = "#stationName + '_' + #hourSlot")
    public CongestionResponse getCongestionByStationAndHour(String stationName, Integer hourSlot) {
        log.info("Fetching congestion data for station: {} at hour: {}", stationName, hourSlot);

        CongestionStatistics statistics =
                congestionStatisticsRepository.findByStationNameAndHourSlot(stationName, hourSlot);

        return statistics != null ? convertToResponse(statistics) : null;
    }

    /**
     * 특정 호선 + 시간대별 혼잡도 조회 (캐싱 적용)
     */
    @Cacheable(value = "congestion", key = "#lineNumber + '_hour_' + #hourSlot")
    public List<CongestionResponse> getCongestionByLineAndHour(String lineNumber, Integer hourSlot) {
        log.info("Fetching congestion data for line: {} at hour: {}", lineNumber, hourSlot);

        List<CongestionStatistics> statistics =
                congestionStatisticsRepository.findByLineNumberAndHourSlotOrderByAvgCongestionDesc(
                        lineNumber, hourSlot);

        return statistics.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 혼잡한 역 TOP 10 조회 (캐싱 적용 - 15분)
     */
    @Cacheable(value = "top10", key = "#hourSlot")
    public List<CongestionResponse> getTopCongestionStations(Integer hourSlot, Integer limit) {
        log.info("Fetching top {} congested stations at hour: {}", limit, hourSlot);

        List<CongestionStatistics> statistics =
                congestionStatisticsRepository.findTopCongestionStations(hourSlot);

        return statistics.stream()
                .limit(limit != null ? limit : 10)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 특정 역의 일별 트렌드 조회 (최근 N일) - 캐싱 적용
     */
    @Cacheable(value = "trend", key = "#stationName + '_' + #days")
    public List<TrendResponse> getDailyTrend(String stationName, Integer days) {
        log.info("Fetching daily trend for station: {} (last {} days)", stationName, days);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days != null ? days : 7);

        List<DailyCongestionTrend> trends =
                dailyCongestionTrendRepository.findByStationNameAndDateBetweenOrderByDateDesc(
                        stationName, startDate, endDate);

        return trends.stream()
                .map(this::convertToTrendResponse)
                .collect(Collectors.toList());
    }

    /**
     * 특정 호선의 일별 트렌드 조회 - 캐싱 적용
     */
    @Cacheable(value = "trend", key = "#lineNumber + '_trend_' + #days")
    public List<TrendResponse> getLineTrend(String lineNumber, Integer days) {
        log.info("Fetching daily trend for line: {} (last {} days)", lineNumber, days);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days != null ? days : 7);

        List<DailyCongestionTrend> trends =
                dailyCongestionTrendRepository.findLineNumberTrend(lineNumber, startDate, endDate);

        return trends.stream()
                .map(this::convertToTrendResponse)
                .collect(Collectors.toList());
    }

    /**
     * 전체 통계 요약 정보 조회
     */
    public long getTotalStatisticsCount() {
        return congestionStatisticsRepository.count();
    }

    /**
     * Entity -> DTO 변환
     */
    private CongestionResponse convertToResponse(CongestionStatistics statistics) {
        return CongestionResponse.builder()
                .lineNumber(statistics.getLineNumber())
                .stationName(statistics.getStationName())
                .hourSlot(statistics.getHourSlot())
                .avgCongestion(statistics.getAvgCongestion())
                .maxCongestion(statistics.getMaxCongestion())
                .minCongestion(statistics.getMinCongestion())
                .dataCount(statistics.getDataCount())
                .build();
    }

    /**
     * Entity -> TrendDTO 변환
     */
    private TrendResponse convertToTrendResponse(DailyCongestionTrend trend) {
        return TrendResponse.builder()
                .lineNumber(trend.getLineNumber())
                .stationName(trend.getStationName())
                .date(trend.getDate())
                .avgCongestion(trend.getAvgCongestion())
                .peakHour(trend.getPeakHour())
                .peakCongestion(trend.getPeakCongestion())
                .build();
    }
}