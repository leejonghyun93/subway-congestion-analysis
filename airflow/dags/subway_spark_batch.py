from datetime import datetime, timedelta
from airflow import DAG
from airflow.operators.bash import BashOperator
from airflow.operators.python import PythonOperator

default_args = {
    'owner': 'subway-admin',
    'start_date': datetime(2025, 1, 1),
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
}

dag = DAG(
    'subway_spark_batch',
    default_args=default_args,
    description='Spark 배치 ETL',
    schedule_interval='0 3 * * *',  # 매일 새벽 3시
    catchup=False,
    tags=['subway', 'spark', 'etl'],
)

# Spark Submit으로 실행
spark_etl_task = BashOperator(
    task_id='run_spark_etl',
    bash_command="""
        docker exec subway-spark-master \
        spark-submit \
        --master spark://spark-master:7077 \
        --executor-memory 2g \
        --total-executor-cores 4 \
        /opt/spark-apps/pyspark_processor.py
    """,
    dag=dag,
)

spark_etl_task