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
public class SecondOpenAiService {

    private final WebClient webClient;

    @Value("${openai.second.api.key}")
    private String secondApiKey;

    private final List<Map<String, String>> conversationHistory = new ArrayList<>();

    public SecondOpenAiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        conversationHistory.add(Map.of("role", "system", "content", "You are a summarization assistant."));
    }

    public Mono<String> summarizeText(String text) {
        return webClient.post()
                .uri("/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + secondApiKey)
                .bodyValue(Map.of(
                        "model", "ft:gpt-4o-mini-2024-07-18:personal:paper-summary-v2:AWy7zgTE",
                        "messages", List.of(
                                Map.of("role", "system", "content",
                                        "너는 긴 논문 텍스트를 요약하는 에이전트이다. 다음 텍스트를 바탕으로 최소 5000자 이상의 상세한 요약을 작성해라. " +
                                                "요약에는 연구 목적, 연구 방법, 주요 결과, 결론을 포함해야 한다."),
                                Map.of("role", "user", "content", text)
                        ),
                        "max_tokens", 2000
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                });
    }

    public Mono<String> generateTitle(String content) {
        return webClient.post()
                .uri("/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + secondApiKey)
                .bodyValue(Map.of(
                        "model", "ft:gpt-4o-mini-2024-07-18:personal:paper-summary-v2:AWy7zgTE",
                        "messages", List.of(
                                Map.of("role", "system", "content", "You are a title generation assistant."),
                                Map.of("role", "user", "content", "Generate a one-line title for this content: " + content)
                        ),
                        "max_tokens", 100
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                });
    }
}
