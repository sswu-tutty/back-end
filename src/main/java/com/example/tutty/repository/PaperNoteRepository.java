package com.example.tutty.repository;

import com.example.tutty.domain.PaperNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaperNoteRepository extends JpaRepository<PaperNote, Long> {
    List<PaperNote> findByUserId(Long userId);
}
