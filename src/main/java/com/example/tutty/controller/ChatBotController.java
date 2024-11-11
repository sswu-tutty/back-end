package com.example.tutty.controller;

import com.example.tutty.domain.Conversation;
import com.example.tutty.domain.User;
import com.example.tutty.dto.chat.ConversationResponseDTO;
import com.example.tutty.service.conversation.ConversationService;
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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ChatBotController {

    private final OpenAiService openAiService;
    private final ConversationService conversationService;
    private final UserService userService;

    @Autowired
    public ChatBotController(OpenAiService openAiService,
                             ConversationService conversationService,
                             UserService userService) {
        this.openAiService = openAiService;
        this.conversationService = conversationService;
        this.userService = userService;
    }
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponseDTO>> getEarliestConversationsByChatroom() {
        // 현재 로그인한 사용자 가져오기
        User user = getCurrentUser();

        // 사용자별 가장 오래된 대화 가져오기
        List<Conversation> conversations = conversationService.getEarliestConversationsByChatroomAndUser(user);

        List<ConversationResponseDTO> responseDTOs = conversations.stream()
                .map(conversation -> new ConversationResponseDTO(
                        conversation.getId(),
                        conversation.getChatroomId(),
                        conversation.getQuestion(),
                        conversation.getAnswer(),
                        conversation.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    @PostMapping("/ask")
    public Mono<ResponseEntity<ConversationResponseDTO>> ask(@RequestParam Long chatroomId, @RequestParam String question) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        String userId = authentication.getName();
        User user = userService.getUserByUserId(userId);

        return openAiService.askQuestion(question)
                .flatMap(apiResponse -> {
                    String answer;
                    ObjectMapper objectMapper = new ObjectMapper();

                    try {
                        // 응답이 JSON인지 확인
                        JsonNode rootNode = objectMapper.readTree(apiResponse);
                        answer = rootNode
                                .path("choices")
                                .get(0)
                                .path("message")
                                .path("content")
                                .asText();
                    } catch (Exception e) {
                        // JSON 형식이 아니면 그대로 문자열로 간주
                        answer = apiResponse;
                    }

                    // 사용자와 chatroomId에 따라 Conversation 생성 및 저장
                    Conversation conversation = new Conversation();
                    conversation.setChatroomId(chatroomId);
                    conversation.setQuestion(question);
                    conversation.setAnswer(answer);
                    conversation.setCreatedAt(LocalDateTime.now());
                    conversation.setUser(user);

                    Conversation savedConversation = conversationService.saveConversation(conversation);

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
    @GetMapping("/conversations/all")
    public ResponseEntity<List<ConversationResponseDTO>> getAllConversations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = authentication.getName();
        User user = userService.getUserByUserId(userId);

        List<Conversation> conversations = conversationService.getConversationsByUser(user);
        List<ConversationResponseDTO> responseDTOs = conversations.stream()
                .map(conversation -> new ConversationResponseDTO(
                        conversation.getId(),
                        conversation.getChatroomId(),
                        conversation.getQuestion(),
                        conversation.getAnswer(),
                        conversation.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/conversations/{chatroomId}")
    public ResponseEntity<List<ConversationResponseDTO>> getConversationsByChatroomId(@PathVariable Long chatroomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = authentication.getName();
        User user = userService.getUserByUserId(userId);

        // 사용자와 chatroomId를 함께 확인하여 대화 조회
        List<Conversation> conversations = conversationService.getConversationsByChatroomIdAndUser(chatroomId, user);
        if (conversations.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<ConversationResponseDTO> responseDTOs = conversations.stream()
                .map(conversation -> new ConversationResponseDTO(
                        conversation.getId(),
                        conversation.getChatroomId(),
                        conversation.getQuestion(),
                        conversation.getAnswer(),
                        conversation.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }
        String userId = authentication.getName();
        return userService.getUserByUserId(userId);
    }

}
