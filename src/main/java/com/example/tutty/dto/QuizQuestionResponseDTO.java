package com.example.tutty.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizQuestionResponseDTO {
    private Long id;
    private String questionText;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private int correctOption;

    public QuizQuestionResponseDTO(Long id, String questionText, String option1, String option2, String option3, String option4, int correctOption) {
        this.id = id;
        this.questionText = questionText;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctOption = correctOption;
    }

}
