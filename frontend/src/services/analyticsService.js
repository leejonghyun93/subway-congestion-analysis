import api from './api';

export const analyticsService = {
    // 실시간 혼잡도 조회
    getRealtimeCongestion: (lineNumber, stationName) =>
        api.get(`/api/analytics/realtime/${stationName}`, {
            params: { lineNumber },
        }),

    // 시간대별 통계
    getHourlyStatistics: (lineNumber, stationName) =>
        api.get('/api/analytics/statistics/hourly', {
            params: { lineNumber, stationName },
        }),

    // TOP 혼잡 역
    getTopCongestedStations: (limit = 10) =>
        api.get('/api/analytics/statistics/top-congested', {
            params: { limit },
        }),

    // 일별 통계
    getDailyStatistics: (startDate, endDate) =>
        api.get('/api/analytics/statistics/daily', {
            params: { startDate, endDate },
        }),
};