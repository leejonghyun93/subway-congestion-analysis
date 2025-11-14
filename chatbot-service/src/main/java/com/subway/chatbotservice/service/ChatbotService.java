package com.subway.chatbotservice.service;

import com.subway.chatbotservice.dto.ChatRequest;
import com.subway.chatbotservice.dto.ChatResponse;
import com.subway.chatbotservice.entity.ChatHistory;
import com.subway.chatbotservice.repository.ChatHistoryRepository;
import com.subway.chatbotservice.service.IntentClassifier.IntentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final OllamaService ollamaService;
    private final IntentClassifier intentClassifier;
    private final DataFetchService dataFetchService;
    private final ChatHistoryRepository chatHistoryRepository;

    public ChatResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. 의도 분류
        IntentResult intentResult = intentClassifier.classify(request.getMessage());
        log.info("Intent classified: {}", intentResult.getIntent());

        // 2. 데이터 조회
        Object data = fetchData(intentResult);

        // 3. LLM 프롬프트 생성 및 응답
        String prompt = buildPrompt(request.getMessage(), intentResult, data);
        String response = ollamaService.generate(prompt);

        // 4. 응답 시간 계산
        long responseTime = System.currentTimeMillis() - startTime;

        // 5. 세션 ID 생성
        String sessionId = request.getSessionId() != null ?
                request.getSessionId() : UUID.randomUUID().toString();

        // 6. MongoDB에 저장
        saveChatHistory(request, response, intentResult, sessionId, responseTime);

        // 7. 응답 반환
        return ChatResponse.builder()
                .response(response)
                .intent(intentResult.getIntent())
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now())
                .responseTimeMs(responseTime)
                .build();
    }

    private Object fetchData(IntentResult intentResult) {
        String intent = intentResult.getIntent();
        String lineNumber = intentResult.getLineNumber();
        String stationName = intentResult.getStationName();

        switch (intent) {
            case "REALTIME_CONGESTION":
                return dataFetchService.fetchRealtimeCongestion(lineNumber, stationName);
            case "PREDICTION":
                return dataFetchService.fetchPrediction(lineNumber, stationName);
            case "STATISTICS":
                return dataFetchService.fetchStatistics(lineNumber, stationName);
            case "TOP_CONGESTED":
                return dataFetchService.fetchTopCongested(10);
            default:
                return null;
        }
    }

    private String buildPrompt(String userMessage, IntentResult intentResult, Object data) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 친절한 지하철 혼잡도 안내 챗봇입니다.\n");
        prompt.append("사용자 질문: ").append(userMessage).append("\n\n");

        if (data != null) {
            prompt.append("실시간 데이터:\n");
            prompt.append(data.toString()).append("\n\n");
            prompt.append("위 데이터를 바탕으로 사용자에게 친절하고 자세하게 답변해주세요.");
        } else {
            prompt.append("데이터를 가져올 수 없습니다. 사용자에게 정중하게 알려주세요.");
        }

        return prompt.toString();
    }

    private void saveChatHistory(ChatRequest request, String response,
                                 IntentResult intentResult, String sessionId, long responseTime) {
        ChatHistory history = ChatHistory.builder()
                .sessionId(sessionId)
                .userId(request.getUserId())
                .userMessage(request.getMessage())
                .botResponse(response)
                .intent(intentResult.getIntent())
                .lineNumber(intentResult.getLineNumber())
                .stationName(intentResult.getStationName())
                .timestamp(LocalDateTime.now())
                .responseTimeMs(responseTime)
                .build();

        chatHistoryRepository.save(history);
        log.info("Chat history saved: sessionId={}", sessionId);
    }
}