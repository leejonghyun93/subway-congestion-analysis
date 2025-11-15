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
    'subway_monitoring',
    default_args=default_args,
    description='시스템 모니터링',
    schedule_interval=timedelta(minutes=5),
    catchup=False,
    tags=['subway', 'monitoring'],
)

def check_data_freshness(**kwargs):
    """데이터 신선도 확인 (최근 데이터가 있는지)"""
    try:
        pg_hook = PostgresHook(postgres_conn_id='subway_postgres')

        query = """
            SELECT MAX(timestamp) as latest_timestamp
            FROM congestion_data;
        """

        result = pg_hook.get_first(query)
        latest_time = result[0] if result and result[0] else None

        if not latest_time:
            logging.error("[ERROR] No data found in database")
            return False

        # 최근 15분 이내 데이터가 있는지 확인
        time_diff = datetime.now() - latest_time.replace(tzinfo=None)
        minutes_old = time_diff.total_seconds() / 60

        if minutes_old > 15:
            logging.warning(f"[WARNING] Data is {minutes_old:.1f} minutes old")
            return False
        else:
            logging.info(f"[INFO] Data is fresh ({minutes_old:.1f} minutes old)")
            return True

    except Exception as e:
        logging.error(f"[ERROR] Freshness check failed: {str(e)}")
        raise

def check_service_health(**kwargs):
    """마이크로서비스 상태 확인"""
    services = {
        'Data Collector': 'http://host.docker.internal:8081/actuator/health',
        'Analytics': 'http://host.docker.internal:8083/actuator/health',
        'Chatbot': 'http://host.docker.internal:8085/actuator/health',
    }

    health_status = {}

    for service_name, url in services.items():
        try:
            response = requests.get(url, timeout=5)
            if response.status_code == 200:
                health_status[service_name] = 'UP'
                logging.info(f"[INFO] {service_name}: UP")
            else:
                health_status[service_name] = 'DOWN'
                logging.warning(f"[WARNING] {service_name}: DOWN")
        except Exception as e:
            health_status[service_name] = 'ERROR'
            logging.error(f"[ERROR] {service_name}: {str(e)}")

    return health_status

def check_database_size(**kwargs):
    """데이터베이스 크기 확인"""
    try:
        pg_hook = PostgresHook(postgres_conn_id='subway_postgres')

        query = """
            SELECT
                pg_size_pretty(pg_total_relation_size('congestion_data')) as congestion_size,
                pg_size_pretty(pg_total_relation_size('hourly_stats')) as stats_size,
                pg_size_pretty(pg_database_size(current_database())) as db_size;
        """

        result = pg_hook.get_first(query)

        logging.info(f"[INFO] Congestion Data Size: {result[0]}")
        logging.info(f"[INFO] Stats Data Size: {result[1]}")
        logging.info(f"[INFO] Total Database Size: {result[2]}")

        return {
            'congestion_data': result[0],
            'hourly_stats': result[1],
            'database': result[2]
        }
    except Exception as e:
        logging.error(f"[ERROR] Size check failed: {str(e)}")
        raise

freshness_task = PythonOperator(
    task_id='check_data_freshness',
    python_callable=check_data_freshness,
    dag=dag,
)

health_task = PythonOperator(
    task_id='check_service_health',
    python_callable=check_service_health,
    dag=dag,
)

size_task = PythonOperator(
    task_id='check_database_size',
    python_callable=check_database_size,
    dag=dag,
)

[freshness_task, health_task, size_task]