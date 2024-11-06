package com.example.tutty.repository;

import com.example.tutty.domain.Conversation;
import com.example.tutty.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUser(User user);
    @Query("SELECT c FROM Conversation c WHERE c.createdAt = (SELECT MIN(c2.createdAt) FROM Conversation c2 WHERE c2.chatroomId = c.chatroomId)")
    List<Conversation> findEarliestConversationsByChatroom();
    List<Conversation> findByChatroomIdAndUser(Long chatroomId, User user);

    // chatroomId를 기준으로 모든 Conversation을 조회하는 메소드 추가
    List<Conversation> findByChatroomId(Long chatroomId);
}
