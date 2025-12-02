import psycopg2

conn = psycopg2.connect(
    host='localhost',
    port=5432,
    database='subway_analytics',
    user='postgres',
    password='postgres'
)
cursor = conn.cursor()

print("=" * 60)
print("PostgreSQL 데이터 검증 결과")
print("=" * 60)

# 1. 총 레코드
cursor.execute("SELECT COUNT(*) FROM congestion_data")
print(f"\n[1] 총 레코드: {cursor.fetchone()[0]:,}건")

# 2. 30일 분포
cursor.execute("""
    SELECT DATE(timestamp) as date, COUNT(*) as count
    FROM congestion_data
    GROUP BY DATE(timestamp)
    ORDER BY date
""")
print("\n[2] 날짜별 분포 (30일):")
for row in cursor.fetchall():
    print(f"  {row[0]}: {row[1]:,}건")

# 3. 역별 TOP 10
cursor.execute("""
    SELECT station_name, COUNT(*) as count
    FROM congestion_data
    GROUP BY station_name
    ORDER BY count DESC
    LIMIT 10
""")
print("\n[3] 역별 TOP 10:")
for i, row in enumerate(cursor.fetchall(), 1):
    print(f"  {i}. {row[0]}: {row[1]:,}건")

# 4. 혼잡도 통계
cursor.execute("""
    SELECT
        AVG(congestion_level) as avg,
        MIN(congestion_level) as min,
        MAX(congestion_level) as max,
        COUNT(*) as total
    FROM congestion_data
""")
row = cursor.fetchone()
print("\n[4] 혼잡도 통계:")
print(f"  평균: {row[0]:.2f}%")
print(f"  최소: {row[1]:.2f}%")
print(f"  최대: {row[2]:.2f}%")
print(f"  총 레코드: {row[3]:,}건")

cursor.close()
conn.close()

print("\n" + "=" * 60)
print("완료! 이 화면을 스크린샷으로 찍으세요!")
print("=" * 60)