package com.example.tutty.controller;

import com.example.tutty.domain.Quiz;
import com.example.tutty.dto.QuizResponseDTO;
import com.example.tutty.service.QuizService;
import com.example.tutty.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class QuizController {

    private final QuizService quizService;
    private final AIService aiService;

    @Autowired
    public QuizController(QuizService quizService, AIService aiService) {
        this.quizService = quizService;
        this.aiService = aiService;
    }

    @PostMapping("/quiz/generate/{chatroomId}")
    public ResponseEntity<QuizResponseDTO> generateQuizByChatroom(@PathVariable Long chatroomId) {
        QuizResponseDTO quizResponse = quizService.generateQuizForChatroom(chatroomId);
        return ResponseEntity.status(HttpStatus.CREATED).body(quizResponse);
    }
}
