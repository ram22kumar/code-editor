package com.realtime.code_editor.repository;

import com.realtime.code_editor.model.CodeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends MongoRepository<CodeDocument, String> {
    List<CodeDocument> findByOwnerId(String ownerId);
    List<CodeDocument> findByActiveUserIdsContaining(String userId);
}