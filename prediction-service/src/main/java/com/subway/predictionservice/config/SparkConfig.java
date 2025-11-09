package com.subway.predictionservice.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Slf4j
@Configuration
public class SparkConfig {

    @Value("${spark.app-name}")
    private String appName;

    @Value("${spark.master}")
    private String master;

    @Bean
    @Lazy  // ✅ Lazy 추가 - 필요할 때만 생성
    public SparkConf sparkConf() {
        SparkConf conf = new SparkConf()
                .setAppName(appName)
                .setMaster(master)
                .set("spark.driver.host", "localhost")
                .set("spark.driver.bindAddress", "127.0.0.1")
                .set("spark.ui.enabled", "false")
                .set("spark.sql.warehouse.dir", "file:///D:/subway-congestion-system/spark-warehouse")
                .set("spark.driver.memory", "2g")
                .set("spark.executor.memory", "2g")
                .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");

        log.info("Spark Configuration initialized: appName={}, master={}", appName, master);
        return conf;
    }

    @Bean
    @Lazy
    public SparkSession sparkSession(SparkConf sparkConf) {
        log.info("Creating SparkSession (Lazy initialization)...");
        SparkSession session = SparkSession.builder()
                .config(sparkConf)
                .getOrCreate();

        log.info("SparkSession created successfully");
        return session;
    }
}