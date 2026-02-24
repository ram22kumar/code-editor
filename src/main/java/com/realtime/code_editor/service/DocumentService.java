package com.realtime.code_editor.service;

import com.realtime.code_editor.model.CodeDocument;
import com.realtime.code_editor.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;

    public CodeDocument createDocument(String title, String ownerId, String language) {
        CodeDocument document = CodeDocument.builder()
                .title(title)
                .content("// Welcome to Real-Time Code Editor\n// Start typing!")
                .language(language != null ? language : "javascript")
                .version(0L)
                .ownerId(ownerId)
                .activeUserIds(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return documentRepository.save(document);
    }

    public Optional<CodeDocument> getDocument(String documentId) {
        return documentRepository.findById(documentId);
    }

    public List<CodeDocument> getUserDocuments(String userId) {
        return documentRepository.findByOwnerId(userId);
    }

    public CodeDocument updateContent(String documentId, String content, Long version) {
        CodeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        document.setContent(content);
        document.setVersion(version);
        document.setUpdatedAt(LocalDateTime.now());

        return documentRepository.save(document);
    }

    public void addActiveUser(String documentId, String userId) {
        CodeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getActiveUserIds().contains(userId)) {
            document.getActiveUserIds().add(userId);
            documentRepository.save(document);
        }
    }

    public void removeActiveUser(String documentId, String userId) {
        CodeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        document.getActiveUserIds().remove(userId);
        documentRepository.save(document);
    }

    public boolean deleteDocument(String documentId, String userId) {
        CodeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Only owner can delete
        if (document.getOwnerId().equals(userId)) {
            documentRepository.deleteById(documentId);
            return true;
        }
        return false;
    }
}