package com.subway.analyticsservice.entity;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;

@Table("realtime_congestion")
public class RealtimeCongestion {

    @PrimaryKeyColumn(name = "line_number", type = PrimaryKeyType.PARTITIONED)
    private String lineNumber;

    @PrimaryKeyColumn(name = "station_name", type = PrimaryKeyType.CLUSTERED)
    private String stationName;

    @Column("congestion_level")
    private Double congestionLevel;

    @Column("passenger_count")
    private Integer passengerCount;

    @Column("updated_at")
    private Instant updatedAt;

    // 기본 생성자
    public RealtimeCongestion() {}

    // 전체 생성자
    public RealtimeCongestion(String lineNumber, String stationName,
                              Double congestionLevel, Integer passengerCount, Instant updatedAt) {
        this.lineNumber = lineNumber;
        this.stationName = stationName;
        this.congestionLevel = congestionLevel;
        this.passengerCount = passengerCount;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getLineNumber() { return lineNumber; }
    public void setLineNumber(String lineNumber) { this.lineNumber = lineNumber; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public Double getCongestionLevel() { return congestionLevel; }
    public void setCongestionLevel(Double congestionLevel) { this.congestionLevel = congestionLevel; }

    public Integer getPassengerCount() { return passengerCount; }
    public void setPassengerCount(Integer passengerCount) { this.passengerCount = passengerCount; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}