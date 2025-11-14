package com.subway.chatbotservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_history")
public class ChatHistory {

    @Id
    private String id;

    private String sessionId;
    private String userId;
    private String userMessage;
    private String botResponse;
    private String intent;  // 질의 의도 (혼잡도 조회, 예측, 통계 등)
    private String lineNumber;  // 추출된 호선 번호
    private String stationName;  // 추출된 역 이름
    private LocalDateTime timestamp;
    private Long responseTimeMs;  // 응답 시간
}