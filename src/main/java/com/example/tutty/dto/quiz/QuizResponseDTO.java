package com.example.tutty.dto.quiz;

import lombok.Getter;
import lombok.Setter;

//퀴즈와 관련된 기본 정보를 반환하기 위한 DTO입니다.
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
