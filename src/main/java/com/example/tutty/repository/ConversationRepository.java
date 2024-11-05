package com.example.tutty.repository;

import com.example.tutty.domain.Conversation;
import com.example.tutty.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUser(User user);

    List<Conversation> findByChatroomIdAndUser(Long chatroomId, User user);

    // chatroomId를 기준으로 모든 Conversation을 조회하는 메소드 추가
    List<Conversation> findByChatroomId(Long chatroomId);
}
