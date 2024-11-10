package com.example.tutty.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "Quiz")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private boolean hasAttempted = false;
    private Boolean liked = false;

    public void markAsAttempted() {
        this.hasAttempted = true;
    }

    @Column(name = "created_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    @Column(name = "chatroom_id", nullable = false)
    private Long chatroomId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}