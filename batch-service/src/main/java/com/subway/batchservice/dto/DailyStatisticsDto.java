package com.subway.batchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatisticsDto {
    private LocalDate statisticsDate;
    private String lineNumber;
    private String stationName;
    private Double avgCongestion;
    private Double maxCongestion;
    private Double minCongestion;
    private Integer totalRecords;
    private Integer peakHour;
    private Integer lowHour;
}