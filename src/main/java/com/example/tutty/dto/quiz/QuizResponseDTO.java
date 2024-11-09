package com.example.tutty.dto.quiz;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizResponseDTO {
    private Long quizId;
    private int totalQuestions;
    private int correctCount;
    private String firstQuestionText;
    private boolean hasAttempted;

    public QuizResponseDTO(Long quizId, int totalQuestions, int correctCount, String firstQuestionText, boolean hasAttempted) {
        this.quizId = quizId;
        this.totalQuestions = totalQuestions;
        this.correctCount = correctCount;
        this.firstQuestionText = firstQuestionText;
        this.hasAttempted = hasAttempted;
    }
}
