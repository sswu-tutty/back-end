package com.example.tutty.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "QuizQuestion")
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String questionText;

    @Column(length = 255)
    private String option1;

    @Column(length = 255)
    private String option2;

    @Column(length = 255)
    private String option3;

    @Column(length = 255)
    private String option4;

    private Integer correctOption;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // toString() 메서드 추가
    @Override
    public String toString() {
        return "QuizQuestion{" +
                "id=" + id +
                ", questionText='" + questionText + '\'' +
                ", option1='" + option1 + '\'' +
                ", option2='" + option2 + '\'' +
                ", option3='" + option3 + '\'' +
                ", option4='" + option4 + '\'' +
                ", correctOption=" + correctOption +
                ", quiz=" + (quiz != null ? quiz.getId() : null) +
                '}';
    }
}
