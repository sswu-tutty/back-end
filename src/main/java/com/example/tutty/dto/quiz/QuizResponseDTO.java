package com.example.tutty.dto.quiz;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizResponseDTO {
    private Long quizId;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private String firstQuestionText;
    private boolean hasAttempted;
    private Boolean liked;

    public QuizResponseDTO(Long quizId, Integer totalQuestions, Integer correctAnswers,
                           String firstQuestionText, boolean hasAttempted, Boolean liked) {
        this.quizId = quizId;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.firstQuestionText = firstQuestionText;
        this.hasAttempted = hasAttempted;
        this.liked = liked;
    }
}
