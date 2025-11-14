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

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${ollama.base-url}")
    private String baseUrl;

    @Value("${ollama.model}")
    private String model;

    public String generate(String prompt) {
        log.info("Calling Ollama API with prompt length: {}", prompt.length());

        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(baseUrl)
                    .build();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            String response = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // JSON 파싱
            JsonNode jsonNode = objectMapper.readTree(response);
            String generatedText = jsonNode.get("response").asText();

            log.info("Ollama response received: {} chars", generatedText.length());
            return generatedText;

        } catch (Exception e) {
            log.error("Ollama API call failed: {}", e.getMessage(), e);
            return "죄송합니다. 응답 생성에 실패했습니다. Ollama 서버가 실행 중인지 확인해주세요.";
        }
    }
}