package com.subway.datacollectorservice.repository;

import com.subway.datacollectorservice.model.SubwayRealtimeData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RawDataRepository extends MongoRepository<SubwayRealtimeData, String> {

    List<SubwayRealtimeData> findByStationNameAndTimestampBetween(
            String stationName,
            LocalDateTime start,
            LocalDateTime end
    );

    List<SubwayRealtimeData> findByLineNameOrderByTimestampDesc(String lineName);

    List<SubwayRealtimeData> findByCongestionLevelGreaterThanEqual(Integer level);
}