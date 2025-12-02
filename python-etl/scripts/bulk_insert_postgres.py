import pandas as pd
import psycopg2
from psycopg2.extras import execute_batch
from datetime import datetime

def bulk_insert_to_postgres(csv_path):
    """
    CSV 데이터를 PostgreSQL에 배치 삽입
    """

    print("=" * 60)
    print("PostgreSQL Bulk Insert 시작")
    print("=" * 60)

    # 1. DB 연결
    print("\n[1/5] PostgreSQL 연결 중...")
    try:
        conn = psycopg2.connect(
            host='localhost',
            port=5432,
            database='subway_analytics',
            user='postgres',
            password='postgres'
        )
        cursor = conn.cursor()
        print("[성공] 연결 성공!")

        # 현재 DB 확인
        cursor.execute("SELECT current_database()")
        current_db = cursor.fetchone()[0]
        print(f"현재 연결 DB: {current_db}")

    except Exception as e:
        print(f"[실패] 연결 실패: {e}")
        return

    # 2. CSV 로드
    print("\n[2/5] CSV 파일 로딩...")
    try:
        df = pd.read_csv(csv_path, encoding='utf-8')
        total_records = len(df)
        print(f"[성공] 로딩 완료: {total_records:,}건")
        print(f"CSV 컬럼: {list(df.columns)}")
    except Exception as e:
        print(f"[실패] CSV 로딩 실패: {e}")
        cursor.close()
        conn.close()
        return

    # 3. 테이블 생성 (없을 경우)
    print("\n[3/5] 테이블 확인 및 생성...")
    create_table_sql = """
    CREATE TABLE IF NOT EXISTS congestion_data (
        id BIGSERIAL PRIMARY KEY,
        station_name VARCHAR(255) NOT NULL,
        line_number VARCHAR(255) NOT NULL,
        congestion_level FLOAT8 NULL,
        passenger_count INT4 NULL,
        timestamp TIMESTAMP(6) NULL
    );
    """

    try:
        cursor.execute(create_table_sql)
        conn.commit()
        print("[성공] 테이블 준비 완료")

        # 테이블 목록 확인
        cursor.execute("""
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = 'public' AND table_name = 'congestion_data'
        """)
        table_exists = cursor.fetchone()
        print(f"테이블 존재: {table_exists is not None}")

    except Exception as e:
        print(f"[실패] 테이블 생성 실패: {e}")
        cursor.close()
        conn.close()
        return

    # 4. 배치 삽입
    print("\n[4/5] 데이터 삽입 중...")
    print(f"배치 크기: 10,000건씩 처리")

    insert_sql = """
        INSERT INTO congestion_data
        (station_name, line_number, congestion_level, passenger_count, timestamp)
        VALUES (%s, %s, %s, %s, %s)
    """

    batch_data = []
    inserted_count = 0
    batch_size = 10000

    start_time = datetime.now()

    try:
        for idx, row in df.iterrows():
            batch_data.append((
                row['station_name'],
                str(row['line_number']),
                float(row['congestion_level']),
                int(row['passenger_count']),
                row['timestamp']
            ))

            # 10,000건마다 배치 삽입
            if len(batch_data) >= batch_size:
                execute_batch(cursor, insert_sql, batch_data, page_size=1000)
                conn.commit()
                inserted_count += len(batch_data)

                # 진행률 계산
                progress = (inserted_count / total_records) * 100
                elapsed = (datetime.now() - start_time).total_seconds()
                speed = inserted_count / elapsed if elapsed > 0 else 0
                remaining = (total_records - inserted_count) / speed if speed > 0 else 0

                print(f"  진행: {inserted_count:,} / {total_records:,} ({progress:.1f}%) | "
                      f"속도: {speed:.0f} rec/sec | 남은시간: {remaining:.0f}초")

                batch_data = []

        # 남은 데이터 삽입
        if batch_data:
            execute_batch(cursor, insert_sql, batch_data, page_size=1000)
            conn.commit()
            inserted_count += len(batch_data)
            print(f"  최종: {inserted_count:,} / {total_records:,} (100.0%)")

        print("[성공] 삽입 완료!")

    except Exception as e:
        print(f"[실패] 삽입 실패: {e}")
        print(f"에러 상세: {type(e).__name__}")
        conn.rollback()
        cursor.close()
        conn.close()
        return

    # 5. 검증
    print("\n[5/5] 데이터 검증 중...")

    try:
        # 총 레코드 수
        cursor.execute("SELECT COUNT(*) FROM congestion_data")
        total_count = cursor.fetchone()[0]
        print(f"[성공] 총 레코드: {total_count:,}건")

        # 날짜별 분포
        cursor.execute("""
            SELECT DATE(timestamp) as date, COUNT(*) as count
            FROM congestion_data
            GROUP BY DATE(timestamp)
            ORDER BY date
            LIMIT 5
        """)
        print("\n날짜별 데이터 분포 (처음 5일):")
        for row in cursor.fetchall():
            print(f"  {row[0]}: {row[1]:,}건")

        # 역별 TOP 5
        cursor.execute("""
            SELECT station_name, COUNT(*) as count
            FROM congestion_data
            GROUP BY station_name
            ORDER BY count DESC
            LIMIT 5
        """)
        print("\n역별 데이터 TOP 5:")
        for row in cursor.fetchall():
            print(f"  {row[0]}: {row[1]:,}건")

        # 혼잡도 평균
        cursor.execute("""
            SELECT
                AVG(congestion_level) as avg_congestion,
                MIN(congestion_level) as min_congestion,
                MAX(congestion_level) as max_congestion
            FROM congestion_data
        """)
        row = cursor.fetchone()
        print(f"\n혼잡도 통계:")
        print(f"  평균: {row[0]:.2f}%")
        print(f"  최소: {row[1]:.2f}%")
        print(f"  최대: {row[2]:.2f}%")

    except Exception as e:
        print(f"[실패] 검증 실패: {e}")

    # 6. 연결 종료
    cursor.close()
    conn.close()

    # 7. 최종 결과
    end_time = datetime.now()
    total_duration = (end_time - start_time).total_seconds()

    print("\n" + "=" * 60)
    print("완료!")
    print("=" * 60)
    print(f"총 소요 시간: {total_duration:.2f}초 ({total_duration/60:.1f}분)")
    print(f"삽입 속도: {inserted_count/total_duration:.0f} records/sec")
    print(f"최종 레코드: {total_count:,}건")
    print("=" * 60)


if __name__ == '__main__':
    # CSV 파일 경로
    csv_path = 'D:/subway-congestion-system/python-etl/scripts/bulk_data_7million.csv'

    print("PostgreSQL Bulk Insert 스크립트")
    print(f"CSV 파일: {csv_path}")
    print()

    # 실행
    bulk_insert_to_postgres(csv_path)

    print("\n[완료] 2순위 완료!")