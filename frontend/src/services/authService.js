import api from './api';

export const authService = {
    // 회원가입
    signup: (username, email, password, nickname) =>
        api.post('/api/auth/signup', {
            username,
            email,
            password,
            nickname,
        }),

    // 로그인
    login: (username, password) =>
        api.post('/api/auth/login', {
            username,
            password,
        }),

    // 로그아웃
    logout: () => api.post('/api/auth/logout'),

    // 내 프로필 조회
    getMyProfile: () => api.get('/api/users/me'),

    // 설정 조회
    getPreferences: () => api.get('/api/users/preferences'),

    // 설정 업데이트
    updatePreferences: (preferences) =>
        api.put('/api/users/preferences', preferences),
};