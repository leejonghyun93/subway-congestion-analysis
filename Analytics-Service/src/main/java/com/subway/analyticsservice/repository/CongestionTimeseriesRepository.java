package com.subway.analyticsservice.repository;


import com.subway.analyticsservice.entity.CongestionTimeseries;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CongestionTimeseriesRepository extends CassandraRepository<CongestionTimeseries, String> {

    @Query("SELECT * FROM congestion_timeseries WHERE station_name = ?0 AND line_number = ?1 AND date = ?2")
    List<CongestionTimeseries> findByStationAndLineAndDate(String stationName, String lineNumber, LocalDate date);

    @Query("SELECT * FROM congestion_timeseries WHERE station_name = ?0 AND line_number = ?1 AND date = ?2 LIMIT ?3")
    List<CongestionTimeseries> findRecentByStationAndLine(String stationName, String lineNumber, LocalDate date, int limit);
}