package com.example.tutty.dto.quiz;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionResultDTO {
    private Long questionId;
    private String questionText;
    private Integer selectedOption;
    private boolean isCorrect;
    private Integer correctOption;

    public QuestionResultDTO(Long questionId, String questionText, Integer selectedOption, boolean isCorrect, Integer correctOption) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.selectedOption = selectedOption;
        this.isCorrect = isCorrect;
        this.correctOption = correctOption;
    }
}
