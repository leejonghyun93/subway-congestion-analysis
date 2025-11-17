import api from './api';

export const analyticsService = {
    getRealtimeCongestion: (lineNumber, stationName) =>
        api.get(`/api/analytics/realtime/${stationName}`, {
            params: { lineNumber },
        }),

    getHourlyStatistics: (lineNumber, stationName) =>
        api.get('/api/analytics/statistics/hourly', {
            params: { lineNumber, stationName },
        }),

    getTopCongestedStations: (limit = 10) =>
        api.get('/api/analytics/statistics/top-congested', {
            params: { limit },
        }),

    getDailyStatistics: (startDate, endDate) =>
        api.get('/api/analytics/statistics/daily', {
            params: { startDate, endDate },
        }),
};