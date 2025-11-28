from datetime import datetime, timedelta
from airflow import DAG
from airflow.operators.python import PythonOperator
from airflow.providers.postgres.hooks.postgres import PostgresHook
import requests
import logging

default_args = {
    'owner': 'subway-admin',
    'depends_on_past': False,
    'start_date': datetime(2025, 1, 1),
    'email_on_failure': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
}

dag = DAG(
    'subway_data_pipeline',
    default_args=default_args,
    description='지하철 데이터 수집 및 처리',
    schedule_interval=timedelta(minutes=10),
    catchup=False,
    tags=['subway', 'data'],
)

def collect_data(**kwargs):
    """데이터 수집 트리거"""
    try:
        response = requests.post(
            'http://host.docker.internal:8081/api/collector/trigger',
            timeout=30
        )
        logging.info(f"[SUCCESS] Data collected: {response.status_code}")
        return response.json()
    except Exception as e:
        logging.error(f"[ERROR] Collection failed: {str(e)}")
        raise

def check_quality(**kwargs):
    """데이터 품질 검사"""
    try:
        pg_hook = PostgresHook(postgres_conn_id='subway_postgres')
        query = """
            SELECT COUNT(*) FROM congestion_data
            WHERE timestamp > NOW() - INTERVAL '10 minutes'
        """
        result = pg_hook.get_first(query)
        count = result[0] if result else 0

        logging.info(f"[INFO] Recent data count: {count}")

        if count < 5:
            logging.warning(f"[WARNING] Low data count: {count} (expected > 5)")
        else:
            logging.info(f"[SUCCESS] Quality check passed: {count} records")

        return count
    except Exception as e:
        logging.error(f"[ERROR] Quality check failed: {str(e)}")
        raise

def calculate_stats(**kwargs):
    """통계 계산"""
    try:
        pg_hook = PostgresHook(postgres_conn_id='subway_postgres')

        # hourly_stats 테이블 생성 (없으면)
        create_table_query = """
            CREATE TABLE IF NOT EXISTS hourly_stats (
                id SERIAL PRIMARY KEY,
                station_name VARCHAR(100),
                line_number VARCHAR(10),
                hour INT,
                avg_congestion DOUBLE PRECISION,
                record_date DATE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(station_name, line_number, hour, record_date)
            );
        """
        pg_hook.run(create_table_query)

        # 시간대별 통계 계산 및 저장
        insert_stats_query = """
            INSERT INTO hourly_stats (station_name, line_number, hour, avg_congestion, record_date)
            SELECT
                station_name,
                line_number,
                EXTRACT(HOUR FROM timestamp)::INT as hour,
                AVG(congestion_level) as avg_congestion,
                CURRENT_DATE as record_date
            FROM congestion_data
            WHERE timestamp >= CURRENT_DATE
            GROUP BY station_name, line_number, EXTRACT(HOUR FROM timestamp)
            ON CONFLICT (station_name, line_number, hour, record_date)
            DO UPDATE SET
                avg_congestion = EXCLUDED.avg_congestion,
                created_at = CURRENT_TIMESTAMP;
        """
        pg_hook.run(insert_stats_query)

        logging.info("[SUCCESS] Hourly statistics calculated and saved")
        return True
    except Exception as e:
        logging.error(f"[ERROR] Stats calculation failed: {str(e)}")
        raise

def detect_anomalies(**kwargs):
    """이상치 탐지"""
    try:
        pg_hook = PostgresHook(postgres_conn_id='subway_postgres')

        # 평균 대비 2배 이상 혼잡한 역 탐지
        query = """
            WITH avg_congestion AS (
                SELECT AVG(congestion_level) as avg_level
                FROM congestion_data
                WHERE timestamp > NOW() - INTERVAL '1 hour'
            )
            SELECT
                station_name,
                line_number,
                congestion_level,
                timestamp
            FROM congestion_data, avg_congestion
            WHERE timestamp > NOW() - INTERVAL '10 minutes'
              AND congestion_level > avg_level * 2
            ORDER BY congestion_level DESC
            LIMIT 5;
        """

        results = pg_hook.get_records(query)

        if results:
            logging.warning(f"[ALERT] Anomalies detected: {len(results)} stations")
            for row in results:
                logging.warning(f"  - {row[0]} (Line {row[1]}): {row[2]}% at {row[3]}")
        else:
            logging.info("[INFO] No anomalies detected")

        return results
    except Exception as e:
        logging.error(f"[ERROR] Anomaly detection failed: {str(e)}")
        raise

collect_task = PythonOperator(
    task_id='collect_data',
    python_callable=collect_data,
    dag=dag,
)

quality_task = PythonOperator(
    task_id='check_quality',
    python_callable=check_quality,
    dag=dag,
)

stats_task = PythonOperator(
    task_id='calculate_stats',
    python_callable=calculate_stats,
    dag=dag,
)

anomaly_task = PythonOperator(
    task_id='detect_anomalies',
    python_callable=detect_anomalies,
    dag=dag,
)

# Task 의존성
collect_task >> quality_task >> [stats_task, anomaly_task]