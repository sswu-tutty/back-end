package com.example.tutty.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiService.class);

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
        // 로그: 사용자 질문 추가
        logger.info("Adding user question to conversation history: {}", question);
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
                .doOnSubscribe(subscription -> logger.info("Sending request to OpenAI API for question: {}", question)) // 요청 전 로그
                .doOnNext(response -> logger.info("Received response from OpenAI API: {}", response)) // 응답 성공 로그
                .doOnError(error -> logger.error("Error occurred during OpenAI API request: {}", error.getMessage(), error)) // 에러 로그
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> {
                            boolean shouldRetry = throwable instanceof WebClientResponseException &&
                                    ((WebClientResponseException) throwable).getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
                            if (shouldRetry) {
                                logger.warn("Retrying due to 429 Too Many Requests...");
                            }
                            return shouldRetry;
                        })
                )
                .map(response -> {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String assistantReply = (String) message.get("content");

                    // 로그: OpenAI 응답 추가
                    logger.info("Adding assistant reply to conversation history: {}", assistantReply);
                    conversationHistory.add(Map.of("role", "assistant", "content", assistantReply));
                    return assistantReply;
                });
    }
}
