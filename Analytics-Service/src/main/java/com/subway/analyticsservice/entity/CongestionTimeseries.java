package com.subway.analyticsservice.entity;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;
import java.time.LocalDate;

@Table("congestion_timeseries")
public class CongestionTimeseries {

    @PrimaryKeyColumn(name = "station_name", type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    private String stationName;

    @PrimaryKeyColumn(name = "line_number", type = PrimaryKeyType.PARTITIONED, ordinal = 1)
    private String lineNumber;

    @PrimaryKeyColumn(name = "date", type = PrimaryKeyType.PARTITIONED, ordinal = 2)
    private LocalDate date;

    @PrimaryKeyColumn(name = "timestamp", type = PrimaryKeyType.CLUSTERED, ordinal = 0)
    private Instant timestamp;

    @Column("congestion_level")
    private Double congestionLevel;

    @Column("passenger_count")
    private Integer passengerCount;

    // 기본 생성자
    public CongestionTimeseries() {}

    // 전체 생성자
    public CongestionTimeseries(String stationName, String lineNumber, LocalDate date,
                                Instant timestamp, Double congestionLevel, Integer passengerCount) {
        this.stationName = stationName;
        this.lineNumber = lineNumber;
        this.date = date;
        this.timestamp = timestamp;
        this.congestionLevel = congestionLevel;
        this.passengerCount = passengerCount;
    }

    // Getters and Setters
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getLineNumber() { return lineNumber; }
    public void setLineNumber(String lineNumber) { this.lineNumber = lineNumber; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public Double getCongestionLevel() { return congestionLevel; }
    public void setCongestionLevel(Double congestionLevel) { this.congestionLevel = congestionLevel; }

    public Integer getPassengerCount() { return passengerCount; }
    public void setPassengerCount(Integer passengerCount) { this.passengerCount = passengerCount; }
}