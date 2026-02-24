package com.realtime.code_editor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChangeDTO {

    private String documentId;
    private String userId;
    private String username;
    private String content;
    private TextOperation operation;
    private Long version;      // Changed from long to Long
    private Long timestamp;    // Changed from long to Long
}