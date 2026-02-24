package com.realtime.code_editor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeChangeDTO {

    private String userId;
    private String username;
    private String content;
    private Integer cursorPosition;  // Changed from int to Integer
    private String color;
    private Long timestamp;           // Changed from long to Long
}