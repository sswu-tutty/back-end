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

import java.time.LocalDateTime;
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

    @GetMapping("/notes")
    public ResponseEntity<List<NoteResponseDTO>> getAllNotes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        User user = userService.getUserByUserId(userId);

        List<Note> notes = noteService.getNotesByUser(user);
        List<NoteResponseDTO> noteDTOs = notes.stream().map(note -> {
            NoteResponseDTO dto = new NoteResponseDTO();
            dto.setId(note.getId());
            dto.setTitle(note.getTitle());
            dto.setContent(note.getContent());
            dto.setCreatedAt(note.getCreatedAt());
            dto.setUpdatedAt(note.getUpdatedAt());
            dto.setLiked(note.getLiked());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(noteDTOs);
    }

    @GetMapping("/notes/{noteId}")
    public ResponseEntity<NoteResponseDTO> getNoteById(@PathVariable Long noteId) {
        Note note = noteService.getNoteById(noteId);
        if (note == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        NoteResponseDTO responseDTO = new NoteResponseDTO();
        responseDTO.setId(note.getId());
        responseDTO.setTitle(note.getTitle());
        responseDTO.setContent(note.getContent());
        responseDTO.setCreatedAt(note.getCreatedAt());
        responseDTO.setUpdatedAt(note.getUpdatedAt());
        responseDTO.setLiked(note.getLiked());

        return ResponseEntity.ok(responseDTO);
    }
    @PutMapping("/notes/{noteId}")
    public ResponseEntity<NoteResponseDTO> updateNote(@PathVariable Long noteId, @RequestBody NoteResponseDTO updatedNote) {
        Note existingNote = noteService.getNoteById(noteId);
        if (existingNote == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        existingNote.setTitle(updatedNote.getTitle());
        existingNote.setContent(updatedNote.getContent());
        existingNote.setUpdatedAt(LocalDateTime.now()); // Only update `updatedAt`

        Note savedNote = noteService.saveNote(existingNote);

        NoteResponseDTO responseDTO = new NoteResponseDTO();
        responseDTO.setId(savedNote.getId());
        responseDTO.setTitle(savedNote.getTitle());
        responseDTO.setContent(savedNote.getContent());
        responseDTO.setCreatedAt(savedNote.getCreatedAt()); // Keep original `createdAt`
        responseDTO.setUpdatedAt(savedNote.getUpdatedAt());
        responseDTO.setLiked(savedNote.getLiked());

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long noteId) {
        Note existingNote = noteService.getNoteById(noteId);
        if (existingNote == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        noteService.deleteNoteById(noteId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/notes/{noteId}/bookmark")
    public ResponseEntity<NoteResponseDTO> toggleBookmark(@PathVariable Long noteId) {
        Note note = noteService.getNoteById(noteId);
        if (note == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Toggle the `liked` status
        note.setLiked(!note.getLiked());
        Note updatedNote = noteService.saveNote(note);

        // Convert to response DTO
        NoteResponseDTO responseDTO = new NoteResponseDTO();
        responseDTO.setId(updatedNote.getId());
        responseDTO.setTitle(updatedNote.getTitle());
        responseDTO.setContent(updatedNote.getContent());
        responseDTO.setCreatedAt(updatedNote.getCreatedAt());
        responseDTO.setUpdatedAt(updatedNote.getUpdatedAt());
        responseDTO.setLiked(updatedNote.getLiked());

        return ResponseEntity.ok(responseDTO);
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
