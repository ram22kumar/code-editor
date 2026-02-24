package com.realtime.code_editor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
// import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "code_documents")
@Data
@Builder
// @NoArgsConstructor
@AllArgsConstructor
public class CodeDocument {

    @Id
    private String id;

    private String title;
    private String content;
    private String language; // javascript, python, java, etc.

    private Long version; // For Operational Transformation

    private String ownerId;
    private List<String> activeUserIds;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CodeDocument() {
        this.content = "// Welcome to Real-Time Code Editor\n// Start typing!";
        this.language = "javascript";
        this.version = 0L;
        this.activeUserIds = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}