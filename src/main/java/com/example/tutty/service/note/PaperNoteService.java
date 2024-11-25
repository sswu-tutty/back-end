package com.example.tutty.service.note;


import com.example.tutty.domain.PaperNote;
import com.example.tutty.domain.User;
import com.example.tutty.repository.PaperNoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}