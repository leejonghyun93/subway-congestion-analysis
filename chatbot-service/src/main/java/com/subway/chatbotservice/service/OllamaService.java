package com.subway.chatbotservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaService {

    private final WebClient ollamaWebClient;
    private final ObjectMapper objectMapper;

    @Value("${ollama.model}")
    private String model;

    @Value("${ollama.enabled:true}")
    private boolean ollamaEnabled;

    /**
     * Ollama로 텍스트 생성
     */
    public String generate(String prompt) {
        if (!ollamaEnabled) {
            log.warn("Ollama is disabled in this environment.");
            return "개발 환경에서만 Ollama를 사용할 수 있습니다.";
        }

        log.info("Calling Ollama API with prompt length: {}", prompt.length());

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            String response = ollamaWebClient.post()
                    .uri("/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) {
                log.warn("Ollama API returned null response.");
                return "응답 생성에 실패했습니다.";
            }

            JsonNode jsonNode = objectMapper.readTree(response);
            if (jsonNode.has("response")) {
                String generatedText = jsonNode.get("response").asText();
                log.info("Ollama response received: {} chars", generatedText.length());
                return generatedText;
            } else {
                log.warn("Ollama response JSON does not contain 'response' field: {}", response);
                return "응답 생성에 실패했습니다.";
            }

        } catch (Exception e) {
            log.error("Ollama API call failed: {}", e.getMessage(), e);
            return "죄송합니다. 응답 생성에 실패했습니다. Ollama 서버가 실행 중인지 확인해주세요.";
        }
    }
}
