package com.example.tutty.dto.quiz;

import lombok.Getter;
import lombok.Setter;

// 질문의 결과를 반환하기 위한 DTO입니다
@Getter
@Setter
public class QuestionResultDTO {
    private Long questionId;
    private String questionText;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private Integer selectedOption;
    private boolean isCorrect;
    private Integer correctOption;

    public QuestionResultDTO(Long questionId, String questionText, String option1, String option2, String option3, String option4,
                             Integer selectedOption, boolean isCorrect, Integer correctOption) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.selectedOption = selectedOption;
        this.isCorrect = isCorrect;
        this.correctOption = correctOption;
    }
}