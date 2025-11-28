package com.subway.analyticsservice.repository;


import com.subway.analyticsservice.entity.RealtimeCongestion;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RealtimeCongestionRepository extends CassandraRepository<RealtimeCongestion, String> {

    @Query("SELECT * FROM realtime_congestion WHERE line_number = ?0")
    List<RealtimeCongestion> findByLineNumber(String lineNumber);

    @Query("SELECT * FROM realtime_congestion WHERE line_number = ?0 AND station_name = ?1")
    Optional<RealtimeCongestion> findByLineNumberAndStationName(String lineNumber, String stationName);
}