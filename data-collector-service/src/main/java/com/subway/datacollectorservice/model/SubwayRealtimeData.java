package com.subway.datacollectorservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "realtime_data")
public class SubwayRealtimeData {

    @Id
    private String id;

    private String stationName;
    private String lineName;
    private String trainNo;
    private String direction;
    private String trainStatus;

    private String previousStation;
    private String nextStation;
    private String arrivalTime;

    private Integer congestionLevel;

    private LocalDateTime timestamp;
    private String rawData;

    private Boolean isExpress;
    private Boolean isLastTrain;
}