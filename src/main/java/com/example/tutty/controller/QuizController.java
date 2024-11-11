package com.example.tutty.controller;

import com.example.tutty.dto.quiz.QuestionResultDTO;
import com.example.tutty.dto.quiz.QuizResponseDTO;
import com.example.tutty.dto.quiz.QuizResultDTO;
import com.example.tutty.service.quiz.QuizService;
import com.example.tutty.service.quiz.QuizAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class QuizController {

    private final QuizService quizService;
    private final QuizAIService quizAiService;

    @Autowired
    public QuizController(QuizService quizService, QuizAIService quizAiService) {
        this.quizService = quizService;
        this.quizAiService = quizAiService;
    }

    @PostMapping("/quiz/generate/{chatroomId}")
    public ResponseEntity<QuizResponseDTO> generateQuizByChatroom(@PathVariable Long chatroomId) {
        QuizResponseDTO quizResponse = quizService.generateQuizForChatroom(chatroomId);
        return ResponseEntity.status(HttpStatus.CREATED).body(quizResponse);
    }

    @PostMapping("/quiz/{quizId}/submit")
    public ResponseEntity<List<QuestionResultDTO>> submitQuizAnswers(@PathVariable Long quizId, @RequestBody Map<Long, Integer> userAnswers) {
        List<QuestionResultDTO> questionResults = quizService.evaluateQuiz(quizId, userAnswers);
        return ResponseEntity.ok(questionResults);
    }

    @GetMapping("/quiz/{quizId}/result")
    public ResponseEntity<QuizResultDTO> getQuizResult(@PathVariable Long quizId) throws AccessDeniedException {
        QuizResultDTO quizResult = quizService.getQuizResult(quizId);
        return ResponseEntity.ok(quizResult);
    }

    @GetMapping("/quiz")
    public List<QuizResponseDTO> getAllQuizzes() {
        return quizService.getAllQuizzesByUser();
    }

    @PatchMapping("/quiz/{quizId}/bookmark")
    public ResponseEntity<QuizResponseDTO> toggleQuizBookmark(@PathVariable Long quizId) {
        QuizResponseDTO quizResponse = quizService.toggleQuizBookmark(quizId);
        return ResponseEntity.ok(quizResponse);
    }

    @DeleteMapping("/quiz/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build(); // 삭제 성공 시 204 No Content 응답
    }

}
