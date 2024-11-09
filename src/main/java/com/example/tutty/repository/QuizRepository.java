package com.example.tutty.repository;

import com.example.tutty.domain.Quiz;
import com.example.tutty.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByUser(User user);
}