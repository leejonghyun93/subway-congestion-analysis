package com.subway.dataprocessorservice.repository;

import com.subway.dataprocessorservice.entity.CongestionData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CongestionDataRepository extends JpaRepository<CongestionData, Long> {

    List<CongestionData> findByStationNameAndLineNumberOrderByTimestampDesc(
            String stationName, String lineNumber);

    List<CongestionData> findByStationNameAndTimestampAfter(
            String stationName, LocalDateTime timestamp);

    CongestionData findTopByStationNameAndLineNumberOrderByTimestampDesc(
            String stationName, String lineNumber);
}