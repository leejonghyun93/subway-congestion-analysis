package com.subway.analyticsservice.service;

import com.subway.analyticsservice.entity.CongestionData;
import com.subway.analyticsservice.repository.CongestionDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final CongestionDataRepository congestionDataRepository;

    /**
     * 실시간 혼잡도 조회
     */
    public CongestionData getRealtimeCongestion(String stationName, String lineNumber) {
        log.info("Fetching realtime congestion for station: {}, line: {}", stationName, lineNumber);

        // "역" 접미사 정규화
        String normalizedStation = stationName;
        if (!normalizedStation.endsWith("역")) {
            normalizedStation = normalizedStation + "역";
        }

        CongestionData data;

        if (lineNumber != null && !lineNumber.isEmpty()) {
            data = congestionDataRepository
                    .findTopByStationNameAndLineNumberOrderByTimestampDesc(normalizedStation, lineNumber);
        } else {
            log.info("lineNumber is null, fetching latest data regardless of line");
            data = congestionDataRepository
                    .findTopByStationNameOrderByTimestampDesc(normalizedStation);
        }

        // "역" 없이도 검색 시도
        if (data == null && normalizedStation.endsWith("역")) {
            String withoutSuffix = normalizedStation.substring(0, normalizedStation.length() - 1);
            if (lineNumber != null && !lineNumber.isEmpty()) {
                data = congestionDataRepository
                        .findTopByStationNameAndLineNumberOrderByTimestampDesc(withoutSuffix, lineNumber);
            } else {
                data = congestionDataRepository
                        .findTopByStationNameOrderByTimestampDesc(withoutSuffix);
            }

            if (data != null) {
                log.info("Found data without suffix: {} - {}%", data.getStationName(), data.getCongestionLevel());
            }
        }

        if (data != null) {
            log.info("Found data: {} - {}%", data.getStationName(), data.getCongestionLevel());
        } else {
            log.warn("No data found for station: {} (normalized: {}), line: {}",
                    stationName, normalizedStation, lineNumber);
        }

        return data;
    }


    /**
     * 시간대별 통계 조회
     */
    public List<Map<String, Object>> getHourlyStatistics(String stationName, String lineNumber) {
        LocalDateTime since = LocalDateTime.now().minusDays(1); // 최근 24시간

        List<CongestionData> dataList;

        if (lineNumber != null && !lineNumber.isEmpty()) {
            // lineNumber까지 조건에 추가
            dataList = congestionDataRepository
                    .findByStationNameAndLineNumberAndTimestampAfterOrderByTimestampAsc(stationName, lineNumber, since);
        } else {
            dataList = congestionDataRepository
                    .findByStationNameAndTimestampAfterOrderByTimestampAsc(stationName, since);
        }

        Map<Integer, List<CongestionData>> hourlyGrouped = dataList.stream()
                .collect(Collectors.groupingBy(d -> d.getTimestamp().getHour()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            List<CongestionData> hourData = hourlyGrouped.getOrDefault(hour, Collections.emptyList());

            Map<String, Object> stat = new HashMap<>();
            stat.put("hour", hour);
            stat.put("avgCongestion", hourData.isEmpty() ? 0.0 :
                    hourData.stream()
                            .mapToDouble(CongestionData::getCongestionLevel)
                            .average()
                            .orElse(0.0));
            stat.put("count", hourData.size());

            result.add(stat);
        }
        return result;
    }



    /**
     * 혼잡도 TOP 역 조회
     */
    public List<Map<String, Object>> getTopCongestedStations(int limit) {
        log.info("Fetching top {} congested stations", limit);

        // 최근 1시간 데이터 조회
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        List<CongestionData> recentData = congestionDataRepository.findByTimestampAfter(since);

        log.info("Found {} records in the last hour", recentData.size());

        if (recentData.isEmpty()) {
            log.warn("No recent data found!");
            return Collections.emptyList();
        }

        // 역별로 그룹화하고 최신 데이터 선택
        Map<String, CongestionData> latestByStation = recentData.stream()
                .collect(Collectors.toMap(
                        CongestionData::getStationName,
                        data -> data,
                        (existing, replacement) ->
                                existing.getTimestamp().isAfter(replacement.getTimestamp())
                                        ? existing : replacement
                ));

        // 혼잡도 기준 정렬
        List<Map<String, Object>> result = latestByStation.values().stream()
                .sorted(Comparator.comparingDouble(CongestionData::getCongestionLevel).reversed())
                .limit(limit)
                .map(data -> {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("stationName", data.getStationName());
                    stat.put("lineNumber", data.getLineNumber());
                    stat.put("congestionLevel", data.getCongestionLevel());
                    stat.put("passengerCount", data.getPassengerCount());
                    stat.put("timestamp", data.getTimestamp());
                    return stat;
                })
                .collect(Collectors.toList());

        log.info("Returning {} top congested stations", result.size());
        result.forEach(stat ->
                log.info("  - {} ({}호선): {}%",
                        stat.get("stationName"),
                        stat.get("lineNumber"),
                        stat.get("congestionLevel"))
        );

        return result;
    }
}