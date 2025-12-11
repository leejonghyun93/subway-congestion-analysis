-- 시간대별 혼잡도 집계 함수
CREATE OR REPLACE FUNCTION calculate_hourly_stats(
    p_date DATE
) RETURNS TABLE(
    hour INT,
    avg_congestion NUMERIC,
    max_congestion NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        EXTRACT(HOUR FROM timestamp)::INT as hour,
        AVG(congestion_level)::NUMERIC(5,2),
        MAX(congestion_level)::NUMERIC(5,2)
    FROM congestion_data
    WHERE DATE(timestamp) = p_date
    GROUP BY EXTRACT(HOUR FROM timestamp)
    ORDER BY hour;
END;
$$ LANGUAGE plpgsql;

-- TOP 혼잡역 조회 함수
CREATE OR REPLACE FUNCTION get_top_congested_stations(
    p_limit INT DEFAULT 10
) RETURNS TABLE(
    station_name VARCHAR,
    line_number INT,
    avg_congestion NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        cd.station_name,
        cd.line_number,
        AVG(cd.congestion_level)::NUMERIC(5,2)
    FROM congestion_data cd
    WHERE cd.timestamp >= NOW() - INTERVAL '1 hour'
    GROUP BY cd.station_name, cd.line_number
    ORDER BY AVG(cd.congestion_level) DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

-- 일일 통계 자동 계산 프로시저
CREATE OR REPLACE PROCEDURE update_daily_statistics(
    p_date DATE
) AS $$
BEGIN
    INSERT INTO daily_statistics (
        date, total_records, avg_congestion, max_congestion
    )
    SELECT 
        p_date,
        COUNT(*),
        AVG(congestion_level),
        MAX(congestion_level)
    FROM congestion_data
    WHERE DATE(timestamp) = p_date
    ON CONFLICT (date) DO UPDATE
    SET total_records = EXCLUDED.total_records,
        avg_congestion = EXCLUDED.avg_congestion,
        max_congestion = EXCLUDED.max_congestion;
END;
$$ LANGUAGE plpgsql;