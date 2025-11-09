-- 데이터베이스는 환경변수로 자동 생성됨
-- 추가 테이블이나 초기 데이터가 필요하면 여기에 작성

-- 예시: 호선 마스터 테이블
CREATE TABLE IF NOT EXISTS subway_lines (
                                            id SERIAL PRIMARY KEY,
                                            line_name VARCHAR(20) UNIQUE NOT NULL,
    line_color VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 초기 데이터 삽입
INSERT INTO subway_lines (line_name, line_color) VALUES
                                                     ('1호선', '#0052A4'),
                                                     ('2호선', '#00A84D'),
                                                     ('3호선', '#EF7C1C'),
                                                     ('4호선', '#00A5DE'),
                                                     ('5호선', '#996CAC'),
                                                     ('6호선', '#CD7C2F'),
                                                     ('7호선', '#747F00'),
                                                     ('8호선', '#E6186C'),
                                                     ('9호선', '#BDB092')
    ON CONFLICT (line_name) DO NOTHING;

-- 역 마스터 테이블
CREATE TABLE IF NOT EXISTS subway_stations (
                                               id SERIAL PRIMARY KEY,
                                               station_name VARCHAR(50) UNIQUE NOT NULL,
    line_name VARCHAR(20),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );