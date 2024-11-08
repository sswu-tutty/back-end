package com.example.tutty.dto.quiz;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizResponseDTO {
    private Long quizId;
    private int totalQuestions;
    private List<QuizQuestionResponseDTO> questions;

    public QuizResponseDTO(Long quizId, int totalQuestions, List<QuizQuestionResponseDTO> questions) {
        this.quizId = quizId;
        this.totalQuestions = totalQuestions;
        this.questions = questions;
    }

    // Getters and Setters
}