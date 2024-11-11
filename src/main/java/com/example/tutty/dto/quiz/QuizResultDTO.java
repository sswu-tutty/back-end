package com.example.tutty.dto.quiz;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

//특정 퀴즈의 결과를 반환하기 위한 DTO입니다
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
