#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ETL 파이프라인 테스트
"""

import pandas as pd
import os

def test_pandas_processor():
    """Pandas ETL 테스트"""
    print("=" * 60)
    print("Pandas ETL 파이프라인 테스트")
    print("=" * 60)

    from pandas_processor import SubwayDataProcessor

    processor = SubwayDataProcessor()

    # 1. 데이터 생성 테스트
    processor.generate_sample_data(1000)
    assert len(processor.df) == 1000, "데이터 생성 실패"
    print("[통과] 데이터 생성: 1000건")

    # 2. 변환 테스트
    processor.transform_add_time_features()
    assert 'hour' in processor.df.columns, "시간 피처 실패"
    print("[통과] 시간 피처 추가")

    processor.transform_add_rush_hour()
    assert 'time_period' in processor.df.columns, "출퇴근 시간 피처 실패"
    print("[통과] 출퇴근 시간 피처 추가")

    processor.transform_classify_congestion()
    assert 'congestion_status' in processor.df.columns, "혼잡도 분류 실패"
    print("[통과] 혼잡도 등급 분류")

    processor.transform_add_rolling_stats(window=5)
    assert 'rolling_mean' in processor.df.columns, "이동 통계 실패"
    print("[통과] 이동 평균/표준편차 추가")

    processor.transform_detect_anomalies(threshold=2.0)
    assert 'is_anomaly' in processor.df.columns, "이상치 탐지 실패"
    print("[통과] 이상치 탐지")

    # 3. 집계 테스트
    station_stats = processor.aggregate_by_station()
    assert len(station_stats) > 0, "역별 집계 실패"
    print(f"[통과] 역별 통계 집계: {len(station_stats)}개 역")

    hourly_stats = processor.aggregate_by_hour()
    assert len(hourly_stats) > 0, "시간대별 집계 실패"
    print(f"[통과] 시간대별 통계 집계: {len(hourly_stats)}시간")

    # 4. 품질 검사 테스트
    quality = processor.check_data_quality()
    assert quality['total'] == 1000, "품질 검사 실패"
    print("[통과] 데이터 품질 검사")

    # 5. 저장 테스트
    processor.save_to_csv('test_output')
    assert os.path.exists('test_output/processed_data.csv'), "CSV 저장 실패"
    print("[통과] CSV 파일 저장")

    # 정리
    import shutil
    if os.path.exists('test_output'):
        shutil.rmtree('test_output')

    print("\n" + "=" * 60)
    print("모든 Pandas 테스트 통과")
    print("=" * 60)


def test_pyspark_syntax():
    """PySpark 코드 구문 테스트 (실행 없이)"""
    print("\n" + "=" * 60)
    print("PySpark 코드 구문 테스트")
    print("=" * 60)

    try:
        from pyspark_processor import SparkETLPipeline
        print("[통과] 모듈 임포트")

        pipeline = SparkETLPipeline("TestETL")
        print("[통과] 클래스 초기화")

        # 모든 메서드 존재 확인
        methods = [
            'create_spark_session',
            'generate_sample_data',
            'transform_add_time_features',
            'transform_add_rush_hour',
            'transform_classify_congestion',
            'transform_add_rolling_stats',
            'transform_detect_anomalies',
            'aggregate_by_station',
            'aggregate_by_hour',
            'aggregate_by_line',
            'check_data_quality',
            'save_to_csv',
            'run_pipeline'
        ]

        for method in methods:
            assert hasattr(pipeline, method), f"메서드 {method} 없음"
        print(f"[통과] 모든 메서드 존재: {len(methods)}개")

        print("\n" + "=" * 60)
        print("PySpark 구문 테스트 통과")
        print("=" * 60)
        print("참고: 전체 실행은 Spark 환경 필요")

    except Exception as e:
        print(f"[실패] 테스트 실패: {e}")


if __name__ == "__main__":
    print("\nETL 파이프라인 테스트 시작\n")

    # 테스트 1: Pandas (전체 실행)
    test_pandas_processor()

    # 테스트 2: PySpark (구문만)
    test_pyspark_syntax()

    print("\n테스트 완료!\n")