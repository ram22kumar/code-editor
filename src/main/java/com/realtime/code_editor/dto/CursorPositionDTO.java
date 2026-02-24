package com.realtime.code_editor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorPositionDTO {

    private String documentId;
    private String userId;
    private String username;
    private String color;
    private Integer line;
    private Integer column;
    private Long timestamp;
}