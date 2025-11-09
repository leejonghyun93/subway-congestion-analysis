package com.subway.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendResponse implements Serializable {

    private String lineNumber;
    private String stationName;
    private LocalDate date;
    private Double avgCongestion;
    private Integer peakHour;
    private Double peakCongestion;
}