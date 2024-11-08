package com.example.tutty.controller;

import com.example.tutty.domain.Conversation;
import com.example.tutty.domain.User;
import com.example.tutty.dto.ConversationResponseDTO;
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
    @GetMapping("/conversations/first-by-chatroom")
    public ResponseEntity<List<ConversationResponseDTO>> getEarliestConversationsByChatroom() {
        List<Conversation> conversations = conversationService.getEarliestConversationsByChatroom();

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
    public Mono<ResponseEntity<ConversationResponseDTO>> ask(@RequestParam Long chatroomId,
                                                             @RequestParam String question) {
        // 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        String userId = authentication.getName();
        User user = userService.getUserByUserId(userId);

        return openAiService.askQuestion(question)
                .flatMap(apiResponse -> {
                    String answer;
                    try {
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

    @GetMapping("/conversations")
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

    // 특정 대화 상세 조회 - 등록된 사용자에 한해서
    @GetMapping("/conversations/{chatroomId}")
    public ResponseEntity<List<ConversationResponseDTO>> getConversationsByChatroomId(@PathVariable Long chatroomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = authentication.getName();
        User user = userService.getUserByUserId(userId);

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

}
