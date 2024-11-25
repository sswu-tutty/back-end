package com.example.tutty.dto.note;

import com.example.tutty.domain.PaperNote;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaperNoteResponseDTO {
    private Long id;
    private String paperTitle;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaperNoteResponseDTO(PaperNote paperNote) {
        this.id = paperNote.getId();
        this.paperTitle = paperNote.getPaperTitle();
        this.content = paperNote.getContent();
        this.createdAt = paperNote.getCreatedAt();
        this.updatedAt = paperNote.getUpdatedAt();
    }
}
