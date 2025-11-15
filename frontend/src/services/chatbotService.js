import api from './api';

export const chatbotService = {
    // 채팅 전송
    sendMessage: (message, sessionId) =>
        api.post('/api/chatbot/chat', {
            message,
            sessionId,
        }),

    // 채팅 이력 조회
    getChatHistory: (sessionId) =>
        api.get(`/api/chatbot/history/${sessionId}`),
};

//  default export도 추가
export default chatbotService;