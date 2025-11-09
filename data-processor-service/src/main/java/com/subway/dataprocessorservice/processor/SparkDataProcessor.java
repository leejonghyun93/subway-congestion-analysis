package com.subway.dataprocessorservice.processor;

import com.subway.dataprocessorservice.model.ProcessedData;
import com.subway.dataprocessorservice.model.SubwayRealtimeData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.apache.spark.sql.functions.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SparkDataProcessor {

    private final SparkSession sparkSession;

    /**
     * 실시간 데이터를 전처리하여 ProcessedData로 변환
     */
    public ProcessedData processRealtimeData(SubwayRealtimeData rawData) {
        LocalDateTime recordTime = rawData.getTimestamp();

        return ProcessedData.builder()
                .stationName(rawData.getStationName())
                .lineName(rawData.getLineName())
                .trainNo(rawData.getTrainNo())
                .direction(rawData.getDirection())
                .congestionLevel(rawData.getCongestionLevel())
                .recordTime(recordTime)
                .processedTime(LocalDateTime.now())
                .hourOfDay(recordTime.getHour())
                .dayOfWeek(recordTime.getDayOfWeek().getValue())
                .isRushHour(isRushHour(recordTime))
                .build();
    }

    /**
     * 배치 데이터 전처리 (Spark 사용)
     */
    public List<ProcessedData> processBatch(List<SubwayRealtimeData> batchData) {
        log.info("Spark 배치 처리 시작: {} 건", batchData.size());

        List<ProcessedData> result = new ArrayList<>();

        try {
            // Java Bean을 Spark Dataset으로 변환
            Dataset<Row> dataset = sparkSession.createDataFrame(batchData, SubwayRealtimeData.class);

            // Spark SQL로 전처리
            Dataset<Row> processed = dataset
                    .withColumn("hour_of_day", hour(col("timestamp")))
                    .withColumn("day_of_week", dayofweek(col("timestamp")))
                    .withColumn("is_rush_hour",
                            when(col("hour_of_day").between(7, 9)
                                    .or(col("hour_of_day").between(18, 20)), true)
                                    .otherwise(false))
                    .select("stationName", "lineName", "trainNo", "direction",
                            "congestionLevel", "timestamp", "hour_of_day",
                            "day_of_week", "is_rush_hour");

            // Dataset을 Java 객체로 변환
            processed.collectAsList().forEach(row -> {
                ProcessedData data = ProcessedData.builder()
                        .stationName(row.getAs("stationName"))
                        .lineName(row.getAs("lineName"))
                        .trainNo(row.getAs("trainNo"))
                        .direction(row.getAs("direction"))
                        .congestionLevel(row.getAs("congestionLevel"))
                        .recordTime(row.getAs("timestamp"))
                        .processedTime(LocalDateTime.now())
                        .hourOfDay(row.getAs("hour_of_day"))
                        .dayOfWeek(row.getAs("day_of_week"))
                        .isRushHour(row.getAs("is_rush_hour"))
                        .build();
                result.add(data);
            });

            log.info("Spark 배치 처리 완료: {} 건", result.size());

        } catch (Exception e) {
            log.error("Spark 처리 실패: {}", e.getMessage(), e);
        }

        return result;
    }

    /**
     * 출퇴근 시간 판별
     */
    private boolean isRushHour(LocalDateTime time) {
        int hour = time.getHour();
        DayOfWeek dayOfWeek = time.getDayOfWeek();

        // 평일 출퇴근 시간
        if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
            return (hour >= 7 && hour <= 9) || (hour >= 18 && hour <= 20);
        }

        return false;
    }
}