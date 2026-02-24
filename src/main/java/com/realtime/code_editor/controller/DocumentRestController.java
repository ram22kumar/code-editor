package com.realtime.code_editor.controller;

import com.realtime.code_editor.model.CodeDocument;
import com.realtime.code_editor.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class DocumentRestController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<CodeDocument> createDocument(
            @RequestParam String title,
            @RequestParam String ownerId,
            @RequestParam(required = false) String language) {

        CodeDocument document = documentService.createDocument(title, ownerId, language);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CodeDocument> getDocument(@PathVariable String id) {
        return documentService.getDocument(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CodeDocument>> getUserDocuments(@PathVariable String userId) {
        return ResponseEntity.ok(documentService.getUserDocuments(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable String id,
            @RequestParam String userId) {

        boolean deleted = documentService.deleteDocument(id, userId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}