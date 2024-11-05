package com.example.tutty.controller;

import com.example.tutty.domain.Conversation;
import com.example.tutty.domain.User;
import com.example.tutty.dto.ConversationResponseDTO;
import com.example.tutty.service.ConversationService;
import com.example.tutty.service.OpenAiService;
import com.example.tutty.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class StudyAssistantController {

    private final OpenAiService openAiService;
    private final ConversationService conversationService;
    private final UserService userService;

    @Autowired
    public StudyAssistantController(OpenAiService openAiService,
                                    ConversationService conversationService,
                                    UserService userService) {
        this.openAiService = openAiService;
        this.conversationService = conversationService;
        this.userService = userService;
    }

    @PostMapping("/ask")
    public Mono<ResponseEntity<ConversationResponseDTO>> ask(@RequestParam Long chatroomId,
                                                             @RequestParam String question) {
        // 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        // 사용자 정보 가져오기
        User user = userService.getUserByUserId(userId);

        // 질문을 OpenAI API에 전달하여 답변 받기
        return openAiService.askQuestion(question)
                .flatMap(apiResponse -> {
                    // JSON 응답에서 "content" 필드의 응답 내용만 추출
                    String answer;
                    try {
                        // JSON 파싱을 위한 라이브러리 사용
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = objectMapper.readTree(apiResponse);
                        answer = rootNode
                                .path("choices")
                                .get(0)
                                .path("message")
                                .path("content")
                                .asText();
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to parse OpenAI response", e));
                    }

                    // Conversation 엔티티 생성 및 데이터 저장
                    Conversation conversation = new Conversation();
                    conversation.setChatroomId(chatroomId);
                    conversation.setQuestion(question);
                    conversation.setAnswer(answer); // 추출된 답변 내용만 저장
                    conversation.setCreatedAt(LocalDateTime.now());
                    conversation.setUser(user);

                    // Conversation 저장
                    Conversation savedConversation = conversationService.saveConversation(conversation);

                    // ConversationResponseDTO로 변환하여 응답
                    ConversationResponseDTO responseDTO = new ConversationResponseDTO(
                            savedConversation.getId(),
                            savedConversation.getChatroomId(),
                            savedConversation.getQuestion(),
                            savedConversation.getAnswer(),
                            savedConversation.getCreatedAt()
                    );

                    return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(responseDTO));
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }


}
