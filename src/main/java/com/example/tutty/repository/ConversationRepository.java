package com.example.tutty.repository;

import com.example.tutty.domain.Conversation;
import com.example.tutty.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUser(User user);
    Optional<Conversation> findByIdAndUser(Long id, User user);
}