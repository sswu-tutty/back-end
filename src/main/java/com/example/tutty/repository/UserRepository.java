package com.example.tutty.repository;

import com.example.tutty.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 특정 사용자 ID로 사용자를 조회하는 메서드
    Optional<User> findByUserId(String userId);

    // 사용자 ID가 존재하는지 확인하는 메서드
    boolean existsByUserId(String userId);
}