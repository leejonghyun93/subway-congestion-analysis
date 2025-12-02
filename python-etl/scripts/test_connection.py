import psycopg2

conn = psycopg2.connect(
    host='localhost',
    port=5432,
    database='subway_analytics',
    user='postgres',
    password='postgres'
)
cursor = conn.cursor()

# 현재 DB
cursor.execute("SELECT current_database()")
print(f"현재 DB: {cursor.fetchone()[0]}")

# 테이블 목록
cursor.execute("SELECT tablename FROM pg_tables WHERE schemaname='public'")
print(f"테이블 목록: {cursor.fetchall()}")

# 데이터 확인
try:
    cursor.execute("SELECT COUNT(*) FROM congestion_data")
    print(f"총 레코드: {cursor.fetchone()[0]:,}건")
except Exception as e:
    print(f"에러: {e}")

cursor.close()
conn.close()