#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
지하철 혼잡도 데이터 처리 - Pandas
"""

import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import logging
import os

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)


class SubwayDataProcessor:

    def __init__(self):
        self.df = None

    def generate_sample_data(self, n_records=10000):
        logger.info(f"샘플 데이터 {n_records}건 생성 중...")
        np.random.seed(42)

        stations = [
            ('강남역', '2'), ('홍대입구역', '2'), ('신림역', '2'),
            ('잠실역', '2'), ('여의도역', '5'), ('서울역', '1'),
            ('종로3가역', '1'), ('신촌역', '2'), ('건대입구역', '2'),
            ('사당역', '2'), ('왕십리역', '2'), ('을지로입구역', '2'),
            ('시청역', '1'), ('명동역', '4'), ('동대문역', '1'),
            ('신도림역', '1'), ('구로디지털단지역', '2'), ('가산디지털단지역', '1'),
            ('판교역', '신분당'), ('광화문역', '5')
        ]

        data = []
        base_time = datetime.now()

        for i in range(n_records):
            station, line = stations[np.random.randint(0, len(stations))]
            hour = np.random.randint(0, 24)

            if hour in [7, 8, 9]:
                congestion = np.random.uniform(65, 95)
            elif hour in [18, 19, 20]:
                congestion = np.random.uniform(60, 90)
            elif hour in [12, 13]:
                congestion = np.random.uniform(45, 65)
            elif hour in [0, 1, 2, 3, 4, 5]:
                congestion = np.random.uniform(5, 20)
            else:
                congestion = np.random.uniform(30, 55)

            if station in ['강남역', '홍대입구역', '잠실역']:
                congestion = min(100, congestion * 1.15)

            timestamp = base_time - timedelta(
                days=np.random.randint(0, 30),
                hours=hour,
                minutes=np.random.randint(0, 60)
            )

            data.append({
                'id': i + 1,
                'station_name': station,
                'line_number': line,
                'congestion_level': round(congestion, 2),
                'passenger_count': int(congestion * np.random.uniform(2.5, 3.5)),
                'timestamp': timestamp
            })

        self.df = pd.DataFrame(data)
        logger.info(f"샘플 데이터 생성 완료: {len(self.df)}건")
        return self.df

    def transform_add_time_features(self):
        logger.info("시간 피처 추가 중...")
        self.df['timestamp'] = pd.to_datetime(self.df['timestamp'])
        self.df['date'] = self.df['timestamp'].dt.date
        self.df['hour'] = self.df['timestamp'].dt.hour
        self.df['minute'] = self.df['timestamp'].dt.minute
        self.df['day_of_week'] = self.df['timestamp'].dt.dayofweek
        self.df['day_name'] = self.df['timestamp'].dt.day_name()
        self.df['is_weekend'] = self.df['day_of_week'].isin([5, 6])
        logger.info("  시간 피처 추가 완료")
        return self.df

    def transform_add_rush_hour(self):
        logger.info("출퇴근 시간 피처 추가 중...")

        def classify_time_period(hour):
            if hour in [7, 8, 9]:
                return '출근'
            elif hour in [18, 19, 20]:
                return '퇴근'
            elif hour in [12, 13]:
                return '점심'
            elif hour in [0, 1, 2, 3, 4, 5]:
                return '심야'
            else:
                return '평시'

        self.df['time_period'] = self.df['hour'].apply(classify_time_period)
        self.df['is_rush_hour'] = self.df['time_period'].isin(['출근', '퇴근'])
        logger.info("  출퇴근 시간 피처 추가 완료")
        return self.df

    def transform_classify_congestion(self):
        logger.info("혼잡도 등급 분류 중...")

        def classify(level):
            if level < 30:
                return 'LOW'
            elif level < 50:
                return 'MEDIUM'
            elif level < 70:
                return 'HIGH'
            else:
                return 'VERY_HIGH'

        self.df['congestion_status'] = self.df['congestion_level'].apply(classify)
        status_map = {'LOW': 1, 'MEDIUM': 2, 'HIGH': 3, 'VERY_HIGH': 4}
        self.df['congestion_grade'] = self.df['congestion_status'].map(status_map)
        logger.info("  혼잡도 등급 분류 완료")
        return self.df

    def transform_add_rolling_stats(self, window=5):
        logger.info(f"이동 통계 추가 중 (window={window})...")
        self.df = self.df.sort_values(['station_name', 'line_number', 'timestamp'])
        grouped = self.df.groupby(['station_name', 'line_number'])

        self.df['rolling_mean'] = grouped['congestion_level'].transform(
            lambda x: x.rolling(window=window, min_periods=1).mean().round(2)
        )
        self.df['rolling_std'] = grouped['congestion_level'].transform(
            lambda x: x.rolling(window=window, min_periods=1).std().round(2)
        )
        self.df['prev_congestion'] = grouped['congestion_level'].shift(1)
        self.df['congestion_change'] = (self.df['congestion_level'] - self.df['prev_congestion']).round(2)
        logger.info("  이동 통계 추가 완료")
        return self.df

    def transform_detect_anomalies(self, threshold=2.0):
        logger.info(f"이상치 탐지 중 (threshold={threshold})...")
        mean = self.df['congestion_level'].mean()
        std = self.df['congestion_level'].std()
        self.df['z_score'] = ((self.df['congestion_level'] - mean) / std).round(3)
        self.df['is_anomaly'] = abs(self.df['z_score']) > threshold
        anomaly_count = self.df['is_anomaly'].sum()
        logger.info(f"  이상치: {anomaly_count}건")
        return self.df

    def aggregate_by_station(self):
        logger.info("역별 통계 집계 중...")
        stats = self.df.groupby(['station_name', 'line_number']).agg({
            'congestion_level': ['mean', 'max', 'min', 'std', 'count'],
            'passenger_count': ['mean', 'sum']
        }).round(2)
        stats.columns = ['avg_congestion', 'max_congestion', 'min_congestion',
                         'std_congestion', 'record_count', 'avg_passengers', 'total_passengers']
        stats = stats.reset_index().sort_values('avg_congestion', ascending=False)
        return stats

    def aggregate_by_hour(self):
        logger.info("시간대별 통계 집계 중...")
        stats = self.df.groupby('hour').agg({
            'congestion_level': ['mean', 'max', 'min'],
            'id': 'count'
        }).round(2)
        stats.columns = ['avg_congestion', 'max_congestion', 'min_congestion', 'record_count']
        stats = stats.reset_index()
        return stats

    def aggregate_by_line(self):
        logger.info("호선별 통계 집계 중...")
        stats = self.df.groupby('line_number').agg({
            'congestion_level': ['mean', 'max', 'min'],
            'station_name': 'nunique',
            'id': 'count'
        }).round(2)
        stats.columns = ['avg_congestion', 'max_congestion', 'min_congestion',
                         'station_count', 'record_count']
        stats = stats.reset_index().sort_values('avg_congestion', ascending=False)
        return stats

    def check_data_quality(self):
        logger.info("데이터 품질 검사 중...")
        total = len(self.df)
        nulls = self.df.isnull().sum().sum()
        duplicates = self.df.duplicated().sum()
        mean_cong = self.df['congestion_level'].mean()

        logger.info(f"  총 레코드: {total}")
        logger.info(f"  결측치: {nulls}")
        logger.info(f"  중복: {duplicates}")
        logger.info(f"  평균 혼잡도: {mean_cong:.2f}%")
        return {'total': total, 'nulls': nulls, 'duplicates': duplicates}

    def save_to_csv(self, output_dir='output'):
        logger.info(f"CSV 저장 중... ({output_dir}/)")
        os.makedirs(output_dir, exist_ok=True)

        self.df.to_csv(f'{output_dir}/processed_data.csv', index=False, encoding='utf-8-sig')
        logger.info("  processed_data.csv 저장 완료")

        station_stats = self.aggregate_by_station()
        station_stats.to_csv(f'{output_dir}/station_stats.csv', index=False, encoding='utf-8-sig')
        logger.info("  station_stats.csv 저장 완료")

        hourly_stats = self.aggregate_by_hour()
        hourly_stats.to_csv(f'{output_dir}/hourly_stats.csv', index=False, encoding='utf-8-sig')
        logger.info("  hourly_stats.csv 저장 완료")

        line_stats = self.aggregate_by_line()
        line_stats.to_csv(f'{output_dir}/line_stats.csv', index=False, encoding='utf-8-sig')
        logger.info("  line_stats.csv 저장 완료")

    def run_pipeline(self):
        logger.info("=" * 60)
        logger.info("Pandas ETL 파이프라인 시작")
        logger.info("=" * 60)

        start_time = datetime.now()

        # 1. Extract
        logger.info("\n[1/4] EXTRACT - 데이터 추출")
        self.generate_sample_data(10000)

        # 2. Transform
        logger.info("\n[2/4] TRANSFORM - 데이터 변환")
        self.transform_add_time_features()
        self.transform_add_rush_hour()
        self.transform_classify_congestion()
        self.transform_add_rolling_stats(window=5)
        self.transform_detect_anomalies(threshold=2.0)

        # 3. Quality Check
        logger.info("\n[3/4] QUALITY CHECK - 품질 검사")
        self.check_data_quality()

        # 4. Load
        logger.info("\n[4/4] LOAD - 데이터 저장")
        self.save_to_csv('output')

        # 결과 출력
        print("\n[역별 통계 TOP 10]")
        print(self.aggregate_by_station().head(10).to_string())

        print("\n[시간대별 통계]")
        print(self.aggregate_by_hour().to_string())

        print("\n[호선별 통계]")
        print(self.aggregate_by_line().to_string())

        end_time = datetime.now()
        duration = (end_time - start_time).total_seconds()

        logger.info("\n" + "=" * 60)
        logger.info(f"파이프라인 완료! ({duration:.2f}초)")
        logger.info(f"처리 건수: {len(self.df)}")
        logger.info("=" * 60)

        return self.df


if __name__ == "__main__":
    processor = SubwayDataProcessor()
    df = processor.run_pipeline()

    print("\n[샘플 데이터]")
    print(df.head(5).to_string())

    print("\n[컬럼 목록]")
    print(df.columns.tolist())