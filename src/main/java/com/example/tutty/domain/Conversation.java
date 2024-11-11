package com.example.tutty.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "Conversation")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 채팅방 식별자 - 동일한 채팅방의 대화들을 그룹화
    @Column(name = "chatroom_id", nullable = false)
    private Long chatroomId;

    // 사용자가 한 질문
    @Column(name = "question", columnDefinition = "TEXT")
    private String question;

    // 챗봇의 답변
    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    // 대화를 요청한 유저
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;
}
