import api from './api';

export const analyticsService = {
    // 실시간 메시지 (챗봇에서 문장으로 보여줄 때)
    getRealtimeCongestionMessage: (lineNumber, stationName) =>
        api.get(`/api/analytics/realtime/${encodeURIComponent(stationName)}/message`, {
            params: { lineNumber },
        }),

    // 실시간 데이터 (UI 화면에서 숫자 보여줄 때)
    getRealtimeCongestionData: (lineNumber, stationName) =>
        api.get(`/api/analytics/realtime/${encodeURIComponent(stationName)}/data`, {
            params: { lineNumber },
        }),

    // 시간대별 통계
    getHourlyStatistics: (stationName, lineNumber) =>
        api.get('/api/analytics/hourly', {
            params: { stationName, lineNumber },
        }),
    // 상위 혼잡역 조회
    getTopCongestedStations: (limit = 10) =>
        api.get('/api/analytics/top-congested', { params: { limit } }),
};
