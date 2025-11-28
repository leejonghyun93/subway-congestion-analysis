from datetime import datetime, timedelta
from airflow import DAG
from airflow.operators.python import PythonOperator
from airflow.providers.postgres.hooks.postgres import PostgresHook
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
    'subway_data_cleanup',
    default_args=default_args,
    description='오래된 데이터 정리',
    schedule_interval='0 2 * * *',  # 매일 새벽 2시
    catchup=False,
    tags=['subway', 'maintenance'],
)

def cleanup_old_data(**kwargs):
    """30일 이상 된 데이터 삭제"""
    try:
        pg_hook = PostgresHook(postgres_conn_id='subway_postgres')

        # 삭제 전 레코드 수 확인
        count_before = pg_hook.get_first("SELECT COUNT(*) FROM congestion_data;")[0]

        # 30일 이상 된 데이터 삭제
        delete_query = """
            DELETE FROM congestion_data
            WHERE timestamp < NOW() - INTERVAL '30 days';
        """
        pg_hook.run(delete_query)

        # 삭제 후 레코드 수 확인
        count_after = pg_hook.get_first("SELECT COUNT(*) FROM congestion_data;")[0]
        deleted = count_before - count_after

        logging.info(f"[INFO] Deleted {deleted} old records")
        logging.info(f"[INFO] Remaining records: {count_after}")

        return {'deleted': deleted, 'remaining': count_after}
    except Exception as e:
        logging.error(f"[ERROR] Cleanup failed: {str(e)}")
        raise

def vacuum_database(**kwargs):
    """데이터베이스 VACUUM"""
    try:
        pg_hook = PostgresHook(postgres_conn_id='subway_postgres')

        # autocommit=True로 connection 가져오기
        conn = pg_hook.get_conn()
        conn.autocommit = True
        cursor = conn.cursor()

        # VACUUM ANALYZE 실행
        cursor.execute("VACUUM ANALYZE congestion_data;")
        cursor.execute("VACUUM ANALYZE hourly_stats;")

        cursor.close()
        conn.close()

        logging.info("[SUCCESS] Database vacuumed successfully")
        return True
    except Exception as e:
        logging.error(f"[ERROR] Vacuum failed: {str(e)}")
        raise

def archive_old_stats(**kwargs):
    """오래된 통계 아카이브"""
    try:
        pg_hook = PostgresHook(postgres_conn_id='subway_postgres')

        # 90일 이상 된 시간별 통계 삭제
        delete_query = """
            DELETE FROM hourly_stats
            WHERE record_date < CURRENT_DATE - INTERVAL '90 days';
        """
        pg_hook.run(delete_query)

        logging.info("[SUCCESS] Old statistics archived")
        return True
    except Exception as e:
        logging.error(f"[ERROR] Archive failed: {str(e)}")
        raise

cleanup_task = PythonOperator(
    task_id='cleanup_old_data',
    python_callable=cleanup_old_data,
    dag=dag,
)

vacuum_task = PythonOperator(
    task_id='vacuum_database',
    python_callable=vacuum_database,
    dag=dag,
)

archive_task = PythonOperator(
    task_id='archive_old_stats',
    python_callable=archive_old_stats,
    dag=dag,
)

cleanup_task >> vacuum_task >> archive_task