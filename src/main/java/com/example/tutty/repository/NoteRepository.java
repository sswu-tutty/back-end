package com.example.tutty.repository;


import com.example.tutty.domain.Note;
import com.example.tutty.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUser(User user);
}