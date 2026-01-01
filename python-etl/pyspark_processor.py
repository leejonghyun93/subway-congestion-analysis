#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from pyspark.sql import SparkSession
from pyspark.sql import functions as F
from pyspark.sql.types import StructType, StructField, StringType, DoubleType, IntegerType, TimestampType
from pyspark.sql.window import Window
from datetime import datetime, timedelta
import random
import logging
import os

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)


class SparkETLPipeline:

    def __init__(self, app_name="SubwayCongestionETL"):
        self.spark = None
        self.df = None
        self.app_name = app_name

    def create_spark_session(self):
        logger.info("Creating Spark session...")
        self.spark = SparkSession.builder \
            .appName(self.app_name) \
            .config("spark.sql.adaptive.enabled", "true") \
            .config("spark.sql.shuffle.partitions", "8") \
            .config("spark.driver.memory", "2g") \
            .getOrCreate()
        self.spark.sparkContext.setLogLevel("WARN")
        logger.info(f"  Spark version: {self.spark.version}")
        return self.spark

    def generate_sample_data(self, n_records=100000):
        logger.info(f"Generating {n_records} sample records...")
        stations = [
            ('Gangnam', '2'), ('Hongdae', '2'), ('Sinlim', '2'),
            ('Jamsil', '2'), ('Yeouido', '5'), ('Seoul', '1'),
            ('Jongno', '1'), ('Sinchon', '2'), ('Kondae', '2'),
            ('Sadang', '2'), ('Wangsimni', '2'), ('Euljiro', '2'),
            ('CityHall', '1'), ('Myeongdong', '4'), ('Dongdaemun', '1'),
            ('Sindorim', '1'), ('Guro', '2'), ('Gasan', '1'),
            ('Pangyo', 'Sinbundang'), ('Gwanghwamun', '5')
        ]
        data = []
        base_time = datetime.now()
        for i in range(n_records):
            station, line = random.choice(stations)
            hour = random.randint(0, 23)
            if hour in [7, 8, 9]:
                congestion = random.uniform(65, 95)
            elif hour in [18, 19, 20]:
                congestion = random.uniform(60, 90)
            elif hour in [12, 13]:
                congestion = random.uniform(45, 65)
            elif hour in [0, 1, 2, 3, 4, 5]:
                congestion = random.uniform(5, 20)
            else:
                congestion = random.uniform(30, 55)
            if station in ['Gangnam', 'Hongdae', 'Jamsil']:
                congestion = min(100.0, congestion * 1.15)
            timestamp = base_time - timedelta(days=random.randint(0, 30), hours=hour, minutes=random.randint(0, 60))
            data.append((i + 1, station, line, round(congestion, 2), int(congestion * random.uniform(2.5, 3.5)), timestamp))
        schema = StructType([
            StructField("id", IntegerType(), False),
            StructField("station_name", StringType(), False),
            StructField("line_number", StringType(), False),
            StructField("congestion_level", DoubleType(), False),
            StructField("passenger_count", IntegerType(), True),
            StructField("timestamp", TimestampType(), False)
        ])
        self.df = self.spark.createDataFrame(data, schema)
        logger.info(f"  Generated {n_records} records")
        return self.df

    def transform_add_time_features(self):
        logger.info("Adding time features...")
        self.df = self.df \
            .withColumn("date", F.to_date(F.col("timestamp"))) \
            .withColumn("hour", F.hour(F.col("timestamp"))) \
            .withColumn("day_of_week", F.dayofweek(F.col("timestamp"))) \
            .withColumn("is_weekend", F.when(F.col("day_of_week").isin([1, 7]), True).otherwise(False))
        logger.info("  Time features added")
        return self.df

    def transform_add_rush_hour(self):
        logger.info("Adding rush hour features...")
        self.df = self.df.withColumn("time_period",
            F.when(F.col("hour").isin([7, 8, 9]), "morning_rush")
            .when(F.col("hour").isin([18, 19, 20]), "evening_rush")
            .when(F.col("hour").isin([12, 13]), "lunch")
            .when(F.col("hour").isin([0, 1, 2, 3, 4, 5]), "night")
            .otherwise("normal"))
        self.df = self.df.withColumn("is_rush_hour",
            F.when(F.col("time_period").isin(["morning_rush", "evening_rush"]), True).otherwise(False))
        logger.info("  Rush hour features added")
        return self.df

    def transform_classify_congestion(self):
        logger.info("Classifying congestion levels...")
        self.df = self.df.withColumn("congestion_status",
            F.when(F.col("congestion_level") < 30, "LOW")
            .when(F.col("congestion_level") < 50, "MEDIUM")
            .when(F.col("congestion_level") < 70, "HIGH")
            .otherwise("VERY_HIGH"))
        self.df = self.df.withColumn("congestion_grade",
            F.when(F.col("congestion_status") == "LOW", 1)
            .when(F.col("congestion_status") == "MEDIUM", 2)
            .when(F.col("congestion_status") == "HIGH", 3)
            .otherwise(4))
        logger.info("  Congestion classified")
        return self.df

    def transform_add_rolling_stats(self, window_size=5):
        logger.info(f"Adding rolling stats (window={window_size})...")
        window_spec = Window.partitionBy("station_name", "line_number").orderBy("timestamp").rowsBetween(-window_size + 1, 0)
        self.df = self.df \
            .withColumn("rolling_mean", F.round(F.avg("congestion_level").over(window_spec), 2)) \
            .withColumn("rolling_max", F.max("congestion_level").over(window_spec)) \
            .withColumn("rolling_min", F.min("congestion_level").over(window_spec))
        lag_window = Window.partitionBy("station_name", "line_number").orderBy("timestamp")
        self.df = self.df \
            .withColumn("prev_congestion", F.lag("congestion_level", 1).over(lag_window)) \
            .withColumn("congestion_change", F.round(F.col("congestion_level") - F.col("prev_congestion"), 2))
        logger.info("  Rolling stats added")
        return self.df

    def transform_detect_anomalies(self, threshold=2.0):
        logger.info(f"Detecting anomalies (threshold={threshold})...")
        stats = self.df.select(F.mean("congestion_level").alias("mean"), F.stddev("congestion_level").alias("std")).collect()[0]
        mean_val, std_val = stats["mean"], stats["std"]
        self.df = self.df \
            .withColumn("z_score", F.round((F.col("congestion_level") - F.lit(mean_val)) / F.lit(std_val), 3)) \
            .withColumn("is_anomaly", F.abs(F.col("z_score")) > threshold)
        anomaly_count = self.df.filter(F.col("is_anomaly") == True).count()
        logger.info(f"  Anomalies found: {anomaly_count}")
        return self.df

    def aggregate_by_station(self):
        logger.info("Aggregating by station...")
        return self.df.groupBy("station_name", "line_number").agg(
            F.round(F.avg("congestion_level"), 2).alias("avg_congestion"),
            F.round(F.max("congestion_level"), 2).alias("max_congestion"),
            F.count("*").alias("record_count")
        ).orderBy(F.desc("avg_congestion"))

    def aggregate_by_hour(self):
        logger.info("Aggregating by hour...")
        return self.df.groupBy("hour").agg(
            F.round(F.avg("congestion_level"), 2).alias("avg_congestion"),
            F.round(F.max("congestion_level"), 2).alias("max_congestion"),
            F.count("*").alias("record_count")
        ).orderBy("hour")

    def aggregate_by_line(self):
        logger.info("Aggregating by line...")
        return self.df.groupBy("line_number").agg(
            F.round(F.avg("congestion_level"), 2).alias("avg_congestion"),
            F.countDistinct("station_name").alias("station_count"),
            F.count("*").alias("record_count")
        ).orderBy(F.desc("avg_congestion"))

    def check_data_quality(self):
        logger.info("Checking data quality...")
        total_count = self.df.count()
        stats = self.df.select(F.mean("congestion_level").alias("mean"), F.stddev("congestion_level").alias("std")).collect()[0]
        logger.info(f"  Total records: {total_count}")
        logger.info(f"  Avg congestion: {stats['mean']:.2f}%")
        return {'total_count': total_count, 'mean': round(stats['mean'], 2)}

    def save_to_csv(self, output_dir):
        logger.info(f"Saving to CSV... ({output_dir})")
        os.makedirs(output_dir, exist_ok=True)
        self.aggregate_by_station().coalesce(1).write.mode("overwrite").option("header", "true").csv(f"{output_dir}/station_stats")
        self.aggregate_by_hour().coalesce(1).write.mode("overwrite").option("header", "true").csv(f"{output_dir}/hourly_stats")
        self.aggregate_by_line().coalesce(1).write.mode("overwrite").option("header", "true").csv(f"{output_dir}/line_stats")
        logger.info("  Save complete")

    def run_pipeline(self, n_records=100000, output_dir="spark_output"):
        logger.info("=" * 60)
        logger.info("PySpark ETL Pipeline Start")
        logger.info("=" * 60)
        start_time = datetime.now()

        self.create_spark_session()

        logger.info("\n[1/5] EXTRACT")
        self.generate_sample_data(n_records)

        logger.info("\n[2/5] TRANSFORM")
        self.transform_add_time_features()
        self.transform_add_rush_hour()
        self.transform_classify_congestion()
        self.transform_add_rolling_stats(window_size=5)
        self.transform_detect_anomalies(threshold=2.0)

        self.df.cache()

        logger.info("\n[3/5] QUALITY CHECK")
        quality = self.check_data_quality()

        logger.info("\n[4/5] AGGREGATE")
        print("\n[Station Stats TOP 10]")
        self.aggregate_by_station().show(10, truncate=False)
        print("\n[Hourly Stats]")
        self.aggregate_by_hour().show(24, truncate=False)
        print("\n[Line Stats]")
        self.aggregate_by_line().show(truncate=False)

        logger.info("\n[5/5] LOAD")
        self.save_to_csv(output_dir)

        self.df.unpersist()

        end_time = datetime.now()
        duration = (end_time - start_time).total_seconds()
        logger.info("\n" + "=" * 60)
        logger.info(f"Pipeline Complete! ({duration:.2f}s)")
        logger.info(f"Processed: {quality['total_count']} records")
        logger.info("=" * 60)
        return self.df

    def stop(self):
        if self.spark:
            self.spark.stop()
            logger.info("Spark session stopped")


if __name__ == "__main__":
    pipeline = SparkETLPipeline("SubwayCongestionETL")
    try:
        df = pipeline.run_pipeline(n_records=100000, output_dir="spark_output")
        print("\n[Schema]")
        df.printSchema()
        print("\n[Sample Data]")
        df.select("station_name", "line_number", "congestion_level", "time_period", "congestion_status").show(10, truncate=False)
    finally:
        pipeline.stop()