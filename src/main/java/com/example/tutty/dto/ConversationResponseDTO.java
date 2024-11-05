package com.example.tutty.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationResponseDTO {
    private Long id;
    private Long chatroomId;
    private String question;
    private String answer;
    private LocalDateTime createdAt;

    // 필요에 따라 생성자 추가
    public ConversationResponseDTO(Long id, Long chatroomId, String question, String answer, LocalDateTime createdAt) {
        this.id = id;
        this.chatroomId = chatroomId;
        this.question = question;
        this.answer = answer;
        this.createdAt = createdAt;
    }
}
