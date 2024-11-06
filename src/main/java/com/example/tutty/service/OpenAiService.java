package com.example.tutty.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String apiKey;

    public OpenAiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<String> askQuestion(String question) {
        // 질문에 조건을 추가하여 답변을 간결하게 요청
        String modifiedQuestion = question + " 이 질문에 대해 간단하고 150토큰 이내로 답변해 주세요.";

        return webClient.post()
                .uri("/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(Map.of(
                        "model", "gpt-4o-mini", // 모델 이름
                        "messages", List.of(Map.of("role", "user", "content", modifiedQuestion)),
                        "max_tokens", 450 // 응답의 최대 길이를 150 토큰으로 설정
                ))
                .retrieve()
                .bodyToMono(String.class);
    }

}
