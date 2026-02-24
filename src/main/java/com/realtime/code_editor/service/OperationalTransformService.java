package com.realtime.code_editor.service;

import com.realtime.code_editor.dto.TextOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OperationalTransformService {

    /**
     * Apply operation to content
     */
    public String applyOperation(String content, TextOperation operation) {
        try {
            if ("insert".equals(operation.getType())) {
                int pos = Math.min(operation.getPosition(), content.length());
                return content.substring(0, pos) +
                        operation.getText() +
                        content.substring(pos);
            } else if ("delete".equals(operation.getType())) {
                int pos = Math.min(operation.getPosition(), content.length());
                int endPos = Math.min(pos + operation.getLength(), content.length());
                return content.substring(0, pos) + content.substring(endPos);
            }
        } catch (Exception e) {
            log.error("Error applying operation: {}", e.getMessage());
        }
        return content;
    }

    /**
     * Transform operation A against operation B
     * Returns transformed A that can be applied after B
     */
    public TextOperation transform(TextOperation a, TextOperation b) {
        if ("insert".equals(a.getType()) && "insert".equals(b.getType())) {
            return transformInsertInsert(a, b);
        } else if ("insert".equals(a.getType()) && "delete".equals(b.getType())) {
            return transformInsertDelete(a, b);
        } else if ("delete".equals(a.getType()) && "insert".equals(b.getType())) {
            return transformDeleteInsert(a, b);
        } else if ("delete".equals(a.getType()) && "delete".equals(b.getType())) {
            return transformDeleteDelete(a, b);
        }
        return a;
    }

    private TextOperation transformInsertInsert(TextOperation a, TextOperation b) {
        TextOperation result = TextOperation.builder()
                .type("insert")
                .text(a.getText())
                .version(a.getVersion() + 1)
                .build();

        if (a.getPosition() < b.getPosition()) {
            result.setPosition(a.getPosition());
        } else if (a.getPosition() > b.getPosition()) {
            result.setPosition(a.getPosition() + b.getText().length());
        } else {
            // Same position - use version as tiebreaker
            result.setPosition(a.getPosition() + b.getText().length());
        }

        return result;
    }

    private TextOperation transformInsertDelete(TextOperation insert, TextOperation delete) {
        TextOperation result = TextOperation.builder()
                .type("insert")
                .text(insert.getText())
                .version(insert.getVersion() + 1)
                .build();

        if (insert.getPosition() <= delete.getPosition()) {
            result.setPosition(insert.getPosition());
        } else if (insert.getPosition() >= delete.getPosition() + delete.getLength()) {
            result.setPosition(insert.getPosition() - delete.getLength());
        } else {
            result.setPosition(delete.getPosition());
        }

        return result;
    }

    private TextOperation transformDeleteInsert(TextOperation delete, TextOperation insert) {
        TextOperation result = TextOperation.builder()
                .type("delete")
                .length(delete.getLength())
                .version(delete.getVersion() + 1)
                .build();

        if (delete.getPosition() >= insert.getPosition() + insert.getText().length()) {
            result.setPosition(delete.getPosition() + insert.getText().length());
        } else if (delete.getPosition() + delete.getLength() <= insert.getPosition()) {
            result.setPosition(delete.getPosition());
        } else {
            result.setPosition(delete.getPosition() + insert.getText().length());
        }

        return result;
    }

    private TextOperation transformDeleteDelete(TextOperation a, TextOperation b) {
        int aStart = a.getPosition();
        int aEnd = a.getPosition() + a.getLength();
        int bStart = b.getPosition();
        int bEnd = b.getPosition() + b.getLength();

        TextOperation result = TextOperation.builder()
                .type("delete")
                .version(a.getVersion() + 1)
                .build();

        if (aEnd <= bStart) {
            // No overlap
            result.setPosition(a.getPosition());
            result.setLength(a.getLength());
        } else if (aStart >= bEnd) {
            // No overlap
            result.setPosition(a.getPosition() - b.getLength());
            result.setLength(a.getLength());
        } else {
            // Overlapping deletes
            int overlapStart = Math.max(aStart, bStart);
            int overlapEnd = Math.min(aEnd, bEnd);
            int overlapLength = overlapEnd - overlapStart;

            result.setPosition(Math.max(0, aStart - bStart));
            result.setLength(a.getLength() - overlapLength);
        }

        return result;
    }
}