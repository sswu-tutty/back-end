package com.example.tutty.controller;

import com.example.tutty.domain.PaperNote;
import com.example.tutty.domain.User;
import com.example.tutty.dto.note.PaperNoteRequestDTO;
import com.example.tutty.dto.note.PaperNoteResponseDTO;
import com.example.tutty.service.UserService;
import com.example.tutty.service.note.PaperNoteService;
import com.example.tutty.service.SecondOpenAiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/paper-notes")
public class PaperNoteController {

    private final PaperNoteService paperNoteService;
    private final SecondOpenAiService secondOpenAiService;
    private final UserService userService;
    public PaperNoteController(PaperNoteService paperNoteService, SecondOpenAiService secondOpenAiService, UserService userService) {
        this.paperNoteService = paperNoteService;
        this.secondOpenAiService = secondOpenAiService;
        this.userService = userService;
    }

    @PostMapping("/summarize")
    public ResponseEntity<PaperNoteResponseDTO> summarizeTextAndSave(@RequestBody PaperNoteRequestDTO request) {
        User user = getCurrentUser();
        String summarizedContent = secondOpenAiService.summarizeText(request.getText()).block();

        String generatedTitle = secondOpenAiService.generateTitle(summarizedContent).block();

        PaperNote paperNote = new PaperNote();
        paperNote.setContent(summarizedContent);
        paperNote.setPaperTitle(generatedTitle);
        paperNote.setUser(user);

        PaperNote savedPaperNote = paperNoteService.savePaperNote(paperNote);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PaperNoteResponseDTO(savedPaperNote));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userService.getUserByUserId(authentication.getName());
    }
}
