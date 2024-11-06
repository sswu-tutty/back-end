package com.example.tutty.mapping;

import com.example.tutty.domain.Note;
import com.example.tutty.dto.note.NoteResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

    public NoteResponseDTO toDto(Note note) {
        NoteResponseDTO dto = new NoteResponseDTO();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        dto.setLiked(note.getLiked());
        return dto;
    }
}
