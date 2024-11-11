package com.example.tutty.dto.note;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoteRequestDTO {
    private String title;
    private Boolean liked;
}
