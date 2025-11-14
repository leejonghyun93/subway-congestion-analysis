-- PostgreSQL 접속
-- psql -U postgres -d subway_analytics

-- 1. 실시간 혼잡도 데이터
INSERT INTO congestion_data (station_name, line_number, congestion_level, timestamp, passenger_count)
VALUES
    ('강남역', '2', 85.5, NOW(), 1500),
    ('홍대입구역', '2', 72.3, NOW(), 1200),
    ('신림역', '2', 65.0, NOW(), 980),
    ('잠실역', '2', 78.9, NOW(), 1350),
    ('구로디지털단지역', '2', 82.1, NOW(), 1420);

-- 2. 시간대별 통계 데이터
INSERT INTO hourly_statistics (station_name, line_number, hour, avg_congestion, max_congestion, passenger_count, date)
VALUES
    ('강남역', '2', 8, 85.0, 95.0, 1500, CURRENT_DATE),
    ('강남역', '2', 9, 90.0, 98.0, 1800, CURRENT_DATE),
    ('강남역', '2', 18, 88.0, 96.0, 1700, CURRENT_DATE),
    ('홍대입구역', '2', 8, 72.0, 82.0, 1200, CURRENT_DATE),
    ('홍대입구역', '2', 18, 75.0, 85.0, 1300, CURRENT_DATE);

-- 3. 혼잡도 순위 데이터
INSERT INTO top_congested_stations (rank, station_name, line_number, avg_congestion, timestamp)
VALUES
    (1, '강남역', '2', 88.5, NOW()),
    (2, '잠실역', '2', 82.3, NOW()),
    (3, '구로디지털단지역', '2', 78.9, NOW()),
    (4, '홍대입구역', '2', 72.1, NOW()),
    (5, '신림역', '2', 68.5, NOW());