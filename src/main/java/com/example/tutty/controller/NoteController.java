package com.example.tutty.controller;

import com.example.tutty.domain.Conversation;
import com.example.tutty.domain.Note;
import com.example.tutty.domain.User;
import com.example.tutty.dto.note.NoteResponseDTO;
import com.example.tutty.mapping.NoteMapper;
import com.example.tutty.service.*;
import com.example.tutty.service.conversation.ConversationService;
import com.example.tutty.service.note.NoteService;
import com.example.tutty.service.note.NoteSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class NoteController {

    private final ConversationService conversationService;
    private final NoteService noteService;
    private final UserService userService;
    private final NoteSummaryService noteSummaryService;
    private final NoteMapper noteMapper;
    @Autowired
    public NoteController(ConversationService conversationService, NoteService noteService,
                          UserService userService, NoteSummaryService noteSummaryService,
                          NoteMapper noteMapper) {
        this.conversationService = conversationService;
        this.noteService = noteService;
        this.userService = userService;
        this.noteSummaryService = noteSummaryService;
        this.noteMapper = noteMapper;
    }
    @PostMapping("/notes/summary/{chatroomId}")
    public ResponseEntity<NoteResponseDTO> createSummaryNote(@PathVariable Long chatroomId) {
        User user = getCurrentUser();
        List<Conversation> conversations = conversationService.getConversationsByChatroomId(chatroomId);

        if (conversations.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String fullContent = conversations.stream().map(Conversation::getAnswer).collect(Collectors.joining(" "));
        String summarizedContent = noteSummaryService.summarizeContent(fullContent);
        String generatedTitle = noteSummaryService.generateTitle(summarizedContent);

        Note note = new Note();
        note.setTitle(generatedTitle);
        note.setContent(summarizedContent);
        note.setUser(user);
        note.setChatroomId(chatroomId);  // 변경된 부분: chatroomId를 설정합니다.
        note.setLiked(false);

        Note savedNote = noteService.saveNote(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(noteMapper.toDto(savedNote));
    }


    @GetMapping("/notes")
    public ResponseEntity<List<NoteResponseDTO>> getAllNotes() {
        User user = getCurrentUser();
        List<Note> notes = noteService.getNotesByUser(user);
        List<NoteResponseDTO> noteDTOs = notes.stream().map(noteMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(noteDTOs);
    }

    @GetMapping("/notes/{noteId}")
    public ResponseEntity<NoteResponseDTO> getNoteById(@PathVariable Long noteId) {
        Note note = findNoteOrThrow(noteId);
        return ResponseEntity.ok(noteMapper.toDto(note));
    }

    @PutMapping("/notes/{noteId}")
    public ResponseEntity<NoteResponseDTO> updateNote(@PathVariable Long noteId, @RequestBody NoteResponseDTO updatedNote) {
        Note existingNote = findNoteOrThrow(noteId);
        existingNote.setTitle(updatedNote.getTitle());
        existingNote.setContent(updatedNote.getContent());
        existingNote.setUpdatedAt(LocalDateTime.now());

        Note savedNote = noteService.saveNote(existingNote);
        return ResponseEntity.ok(noteMapper.toDto(savedNote));
    }

    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long noteId) {
        findNoteOrThrow(noteId);
        noteService.deleteNoteById(noteId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/notes/{noteId}/bookmark")
    public ResponseEntity<NoteResponseDTO> toggleBookmark(@PathVariable Long noteId) {
        Note note = findNoteOrThrow(noteId);
        note.setLiked(!note.getLiked());
        Note updatedNote = noteService.saveNote(note);
        return ResponseEntity.ok(noteMapper.toDto(updatedNote));
    }

    // 공통 예외 처리 메서드
    private Note findNoteOrThrow(Long noteId) {
        return noteService.getNoteById(noteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userService.getUserByUserId(authentication.getName());
    }

}
