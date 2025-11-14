import api from './api';

export const predictionService = {
    // 현재 시간 예측
    predictNow: (lineNumber, stationName) =>
        api.get('/api/prediction/now', {
            params: { lineNumber, stationName },
        }),

    // 여러 시간대 예측
    predictMultipleHours: (lineNumber, stationName, startHour, endHour) =>
        api.get(`/api/prediction/station/${stationName}/hours`, {
            params: { lineNumber, startHour, endHour },
        }),

    // 모델 메트릭 조회
    getModelMetrics: () => api.get('/api/prediction/model/metrics'),
};