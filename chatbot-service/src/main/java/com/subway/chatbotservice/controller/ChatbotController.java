package com.subway.chatbotservice.controller;

import com.subway.chatbotservice.dto.ApiResponse;
import com.subway.chatbotservice.dto.ChatRequest;
import com.subway.chatbotservice.dto.ChatResponse;
import com.subway.chatbotservice.entity.ChatHistory;
import com.subway.chatbotservice.repository.ChatHistoryRepository;
import com.subway.chatbotservice.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final ChatHistoryRepository chatHistoryRepository;

    /**
     * 채팅 요청
     * POST /api/chatbot/chat
     */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Chat request: {}", request.getMessage());

        try {
            ChatResponse response = chatbotService.chat(request);
            return ResponseEntity.ok(ApiResponse.success("응답 생성 완료", response));
        } catch (Exception e) {
            log.error("Chat failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("챗봇 응답 생성 실패: " + e.getMessage()));
        }
    }

    /**
     * 채팅 이력 조회 (세션별)
     * GET /api/chatbot/history/{sessionId}
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<ApiResponse<List<ChatHistory>>> getHistory(@PathVariable String sessionId) {
        log.info("Get history: sessionId={}", sessionId);

        List<ChatHistory> history = chatHistoryRepository.findBySessionIdOrderByTimestampDesc(sessionId);
        return ResponseEntity.ok(ApiResponse.success("채팅 이력 조회 완료", history));
    }

    /**
     * 사용자별 채팅 이력 조회
     * GET /api/chatbot/history/user/{userId}
     */
    @GetMapping("/history/user/{userId}")
    public ResponseEntity<ApiResponse<List<ChatHistory>>> getUserHistory(@PathVariable String userId) {
        log.info("Get user history: userId={}", userId);

        List<ChatHistory> history = chatHistoryRepository.findByUserIdOrderByTimestampDesc(userId);
        return ResponseEntity.ok(ApiResponse.success("사용자 채팅 이력 조회 완료", history));
    }

    /**
     * 헬스 체크
     * GET /api/chatbot/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Chatbot Service is running", "OK"));
    }
}