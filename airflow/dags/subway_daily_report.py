#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from datetime import datetime, timedelta
from airflow import DAG
from airflow.operators.python import PythonOperator
from airflow.providers.postgres.hooks.postgres import PostgresHook
import logging
import json

default_args = {
    'owner': 'subway-admin',
    'depends_on_past': False,
    'start_date': datetime(2025, 1, 1),
    'email_on_failure': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
}

dag = DAG(
    'subway_daily_report',
    default_args=default_args,
    description='일일 리포트 생성',
    schedule_interval='0 23 * * *',
    catchup=False,
    tags=['subway', 'report'],
)

def generate_daily_summary(**kwargs):
    """일일 요약 통계 생성"""
    try:
        pg_hook = PostgresHook(postgres_conn_id='subway_postgres')

        query = """
            SELECT
                COUNT(DISTINCT station_name) as total_stations,
                COUNT(*) as total_records,
                AVG(congestion_level) as avg_congestion,
                MAX(congestion_level) as max_congestion,
                MIN(congestion_level) as min_congestion
            FROM congestion_data
            WHERE timestamp >= CURRENT_DATE;
        """

        result = pg_hook.get_first(query)

        summary = {
            'date': datetime.now().strftime('%Y-%m-%d'),
            'total_stations': result[0] or 0,
            'total_records': result[1] or 0,
            'avg_congestion': float(result[2]) if result[2] else 0,
            'max_congestion': float(result[3]) if result[3] else 0,
            'min_congestion': float(result[4]) if result[4] else 0
        }

        logging.info("[INFO] Daily Summary:")
        logging.info(json.dumps(summary, indent=2))

        return summary
    except Exception as e:
        logging.error(f"[ERROR] Summary generation failed: {str(e)}")
        raise

def generate_top_congested(**kwargs):
    """혼잡도 TOP 10 역 리포트"""
    try:
        pg_hook = PostgresHook(postgres_conn_id='subway_postgres')

        query = """
            SELECT
                station_name,
                line_number,
                AVG(congestion_level) as avg_congestion,
                MAX(congestion_level) as max_congestion,
                COUNT(*) as data_count
            FROM congestion_data
            WHERE timestamp >= CURRENT_DATE
            GROUP BY station_name, line_number
            ORDER BY avg_congestion DESC
            LIMIT 10;
        """

        results = pg_hook.get_records(query)

        top_stations = []
        for idx, row in enumerate(results, 1):
            top_stations.append({
                'rank': idx,
                'station': row[0],
                'line': row[1],
                'avg_congestion': float(row[2]),
                'max_congestion': float(row[3]),
                'data_count': row[4]
            })

        logging.info("[INFO] Top 10 Congested Stations:")
        logging.info(json.dumps(top_stations, indent=2, ensure_ascii=False))

        return top_stations
    except Exception as e:
        logging.error(f"[ERROR] Top congested report failed: {str(e)}")
        raise

def generate_hourly_pattern(**kwargs):
    """시간대별 패턴 분석"""
    try:
        pg_hook = PostgresHook(postgres_conn_id='subway_postgres')

        query = """
            SELECT
                EXTRACT(HOUR FROM timestamp)::INT as hour,
                AVG(congestion_level) as avg_congestion,
                COUNT(*) as record_count
            FROM congestion_data
            WHERE timestamp >= CURRENT_DATE
            GROUP BY EXTRACT(HOUR FROM timestamp)
            ORDER BY hour;
        """

        results = pg_hook.get_records(query)

        hourly_pattern = []
        for row in results:
            hourly_pattern.append({
                'hour': row[0],
                'avg_congestion': float(row[1]),
                'record_count': row[2]
            })

        logging.info("[INFO] Hourly Pattern:")
        logging.info(json.dumps(hourly_pattern, indent=2))

        return hourly_pattern
    except Exception as e:
        logging.error(f"[ERROR] Hourly pattern analysis failed: {str(e)}")
        raise

summary_task = PythonOperator(
    task_id='generate_daily_summary',
    python_callable=generate_daily_summary,
    dag=dag,
)

top_task = PythonOperator(
    task_id='generate_top_congested',
    python_callable=generate_top_congested,
    dag=dag,
)

pattern_task = PythonOperator(
    task_id='generate_hourly_pattern',
    python_callable=generate_hourly_pattern,
    dag=dag,
)

[summary_task, top_task, pattern_task]