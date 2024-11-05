package com.example.tutty.controller;

import com.example.tutty.domain.Conversation;
import com.example.tutty.domain.Note;
import com.example.tutty.domain.User;
import com.example.tutty.dto.NoteResponseDTO;
import com.example.tutty.service.ConversationService;
import com.example.tutty.service.NoteService;
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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class NoteController {

    private final ConversationService conversationService;
    private final NoteService noteService;
    private final UserService userService;
    private final OpenAiService openAiService;

    @Autowired
    public NoteController(ConversationService conversationService, NoteService noteService, UserService userService, OpenAiService openAiService) {
        this.conversationService = conversationService;
        this.noteService = noteService;
        this.userService = userService;
        this.openAiService = openAiService;
    }

    @PostMapping("/notes/summary/{chatroomId}")
    public ResponseEntity<NoteResponseDTO> createSummaryNote(@PathVariable Long chatroomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        User user = userService.getUserByUserId(userId);

        List<Conversation> conversations = conversationService.getConversationsByChatroomId(chatroomId);
        if (conversations.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // 대화 내용을 하나의 텍스트로 병합
        String fullContent = conversations.stream()
                .map(Conversation::getAnswer)
                .collect(Collectors.joining(" "));

        String summarizedContent;
        String generatedTitle;
        try {
            // GPT로 요약 요청
            String gptSummaryResponse = openAiService.askQuestion("다음 대화 내용을 요약해줘: " + fullContent).block();
            summarizedContent = extractContent(gptSummaryResponse);

            // GPT로 제목 요청
            String gptTitleResponse = openAiService.askQuestion("다음 요약 내용에 맞는 한 줄 제목을 만들어줘: " + summarizedContent).block();
            generatedTitle = extractContent(gptTitleResponse);
        } catch (Exception e) {
            summarizedContent = "요약 실패: 기본 요약 내용"; // 오류 발생 시 기본 요약
            generatedTitle = "자동 생성된 요약 제목"; // 오류 발생 시 기본 제목
        }

        Note note = new Note();
        note.setTitle(generatedTitle);
        note.setContent(summarizedContent);
        note.setUser(user);
        note.setConversation(conversations.get(0));
        note.setLiked(false); // 기본값으로 false 설정

        Note savedNote = noteService.saveNote(note);

        NoteResponseDTO responseDTO = new NoteResponseDTO();
        responseDTO.setId(savedNote.getId());
        responseDTO.setTitle(savedNote.getTitle());
        responseDTO.setContent(savedNote.getContent());
        responseDTO.setCreatedAt(savedNote.getCreatedAt());
        responseDTO.setUpdatedAt(savedNote.getUpdatedAt());
        responseDTO.setLiked(savedNote.getLiked());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    private String extractContent(String gptResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(gptResponse);
            return rootNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return "추출 실패"; // 실패 시 기본 텍스트
        }
    }
}
