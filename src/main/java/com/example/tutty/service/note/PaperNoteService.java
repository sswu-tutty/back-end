package com.example.tutty.service.note;


import com.example.tutty.domain.PaperNote;
import com.example.tutty.domain.User;
import com.example.tutty.repository.PaperNoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaperNoteService {

    private final PaperNoteRepository paperNoteRepository;

    public PaperNoteService(PaperNoteRepository paperNoteRepository) {
        this.paperNoteRepository = paperNoteRepository;
    }

    public PaperNote savePaperNote(PaperNote paperNote) {
        return paperNoteRepository.save(paperNote);
    }

    public List<PaperNote> getPaperNotesByUser(User user) {
        return paperNoteRepository.findByUserId(user.getId());
    }

    public void deletePaperNoteById(Long id) {
        paperNoteRepository.deleteById(id);
    }

    public Optional<PaperNote> getPaperNoteById(Long id) {
        return paperNoteRepository.findById(id);
    }
}