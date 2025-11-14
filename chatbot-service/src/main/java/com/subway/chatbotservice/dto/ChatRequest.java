package com.subway.chatbotservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "메시지를 입력해주세요")
    private String message;

    private String sessionId;  // 대화 세션 ID
    private String userId;     // 사용자 ID (선택)
}