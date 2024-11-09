package com.example.tutty.dto.quiz;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizResultDTO {

    private int totalQuestions;
    private int correctCount;
    private List<QuestionResultDTO> questionResults;
    private boolean hasAttempted;
    public QuizResultDTO(int totalQuestions, int correctCount, List<QuestionResultDTO> questionResults, boolean hasAttempted) {
        this.totalQuestions = totalQuestions;
        this.correctCount = correctCount;
        this.questionResults = questionResults;
        this.hasAttempted = hasAttempted;
    }

}
