package com.subway.dataprocessorservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubwayRealtimeData {

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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String rawData;

    private Boolean isExpress;
    private Boolean isLastTrain;
}