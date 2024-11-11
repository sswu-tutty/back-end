package com.example.tutty.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String apiKey;

    // 대화 히스토리를 유지하기 위한 리스트
    private final List<Map<String, String>> conversationHistory = new ArrayList<>();

    public OpenAiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        // 시스템 메시지를 대화 히스토리에 추가 (필요에 따라 수정 가능)
        conversationHistory.add(Map.of("role", "system", "content", "You are a helpful assistant."));
    }

    public Mono<String> askQuestion(String question) {
        // 사용자 질문을 대화 히스토리에 추가
        conversationHistory.add(Map.of("role", "user", "content", question));

        return webClient.post()
                .uri("/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(Map.of(
                        "model", "gpt-4-turbo",
                        "messages", conversationHistory,
                        "max_tokens", 550
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    // 응답을 Map으로 변환 후 내용을 추출
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String assistantReply = (String) message.get("content");

                    // 응답 메시지를 대화 히스토리에 추가
                    conversationHistory.add(Map.of("role", "assistant", "content", assistantReply));
                    return assistantReply;
                });
    }
}
