package com.example.tutty.service.note;

import com.example.tutty.domain.Note;
import com.example.tutty.domain.User;
import com.example.tutty.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    @Autowired
    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public Note saveNote(Note note) {
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        return noteRepository.save(note);
    }

    public Optional<Note> getNoteById(Long noteId) {
        return noteRepository.findById(noteId);
    }

    public List<Note> getNotesByUser(User user) {
        return noteRepository.findByUser(user);
    }

    public void deleteNoteById(Long id) {
        noteRepository.deleteById(id);
    }
}
