package com.subway.analyticsservice.service;


import com.subway.analyticsservice.entity.CongestionTimeseries;
import com.subway.analyticsservice.entity.RealtimeCongestion;
import com.subway.analyticsservice.repository.CongestionTimeseriesRepository;
import com.subway.analyticsservice.repository.RealtimeCongestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CassandraService {

    private static final Logger logger = LoggerFactory.getLogger(CassandraService.class);

    @Autowired
    private CongestionTimeseriesRepository timeseriesRepository;

    @Autowired
    private RealtimeCongestionRepository realtimeRepository;

    // 시계열 데이터 저장
    public void saveCongestionData(String stationName, String lineNumber,
                                   Double congestionLevel, Integer passengerCount) {
        CongestionTimeseries data = new CongestionTimeseries(
                stationName,
                lineNumber,
                LocalDate.now(),
                Instant.now(),
                congestionLevel,
                passengerCount
        );
        timeseriesRepository.save(data);
        logger.debug("Saved timeseries data: {} - {}", stationName, congestionLevel);

        // 실시간 테이블도 업데이트
        updateRealtimeCongestion(stationName, lineNumber, congestionLevel, passengerCount);
    }

    // 실시간 데이터 업데이트
    public void updateRealtimeCongestion(String stationName, String lineNumber,
                                         Double congestionLevel, Integer passengerCount) {
        RealtimeCongestion realtime = new RealtimeCongestion(
                lineNumber,
                stationName,
                congestionLevel,
                passengerCount,
                Instant.now()
        );
        realtimeRepository.save(realtime);
        logger.debug("Updated realtime data: {} - {}", stationName, congestionLevel);
    }

    // 특정 역의 오늘 데이터 조회
    public List<CongestionTimeseries> getTodayData(String stationName, String lineNumber) {
        return timeseriesRepository.findByStationAndLineAndDate(stationName, lineNumber, LocalDate.now());
    }

    // 특정 역의 최근 N개 데이터 조회
    public List<CongestionTimeseries> getRecentData(String stationName, String lineNumber, int limit) {
        return timeseriesRepository.findRecentByStationAndLine(stationName, lineNumber, LocalDate.now(), limit);
    }

    // 특정 호선의 실시간 데이터 조회
    public List<RealtimeCongestion> getRealtimeByLine(String lineNumber) {
        return realtimeRepository.findByLineNumber(lineNumber);
    }

    // 특정 역의 실시간 데이터 조회
    public Optional<RealtimeCongestion> getRealtimeByStation(String lineNumber, String stationName) {
        return realtimeRepository.findByLineNumberAndStationName(lineNumber, stationName);
    }
}