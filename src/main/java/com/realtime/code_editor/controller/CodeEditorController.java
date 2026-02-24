package com.realtime.code_editor.controller;

import com.realtime.code_editor.dto.*;
import com.realtime.code_editor.model.CodeDocument;
import com.realtime.code_editor.model.User;
import com.realtime.code_editor.service.DocumentService;
import com.realtime.code_editor.service.OperationalTransformService;
import com.realtime.code_editor.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@Slf4j
@RequiredArgsConstructor
public class CodeEditorController {

    private final SimpMessagingTemplate messagingTemplate;
    private final DocumentService documentService;
    private final UserService userService;
    private final OperationalTransformService otService;

    // Track cursors per document
    private final Map<String, Map<String, CursorPositionDTO>> documentCursors = new ConcurrentHashMap<>();

    /**
     * Handle user joining a document
     */
    @MessageMapping("/document/{documentId}/join")
    public void handleJoin(
            @DestinationVariable Long documentId,
            UserPresenceDTO userPresence) {

        log.info("User {} joining document {}", userPresence.getUsername(), documentId);

        try {
            // Get or create user
            User user = userService.createOrGetUser(userPresence.getUsername());

            // Update presence with actual database user ID
            userPresence.setUserId(user.getId().toString());
            userPresence.setColor(user.getColor());
            userPresence.setOnline(true);

            // Get document and existing users BEFORE adding new user
            CodeDocument document = documentService.getDocument(String.valueOf(documentId))
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            // Get list of currently active users (before adding the new one)
            List<UserPresenceDTO> existingUsers = new ArrayList<>();
            for (String activeUserId : document.getActiveUserIds()) {
                try {
                    User activeUser = userService.getUser(Long.parseLong(activeUserId))
                            .orElse(null);
                    if (activeUser != null) {
                        UserPresenceDTO existingUserPresence = UserPresenceDTO.builder()
                                .userId(activeUser.getId().toString())
                                .username(activeUser.getUsername())
                                .color(activeUser.getColor())
                                .online(true)
                                .build();
                        existingUsers.add(existingUserPresence);
                    }
                } catch (Exception e) {
                    log.warn("Could not load user {}: {}", activeUserId, e.getMessage());
                }
            }

            // Add new user to document
            documentService.addActiveUser(String.valueOf(documentId), user.getId().toString());

            // Send current document state to the joining user
            DocumentChangeDTO syncMessage = DocumentChangeDTO.builder()
                    .documentId(documentId.toString())
                    .content(document.getContent())
                    .version(document.getVersion())
                    .timestamp(System.currentTimeMillis())
                    .build();

            messagingTemplate.convertAndSendToUser(
                    user.getId().toString(),
                    "/queue/document/" + documentId + "/sync",
                    syncMessage
            );

            // CRITICAL: Send list of existing users to the new joiner
            for (UserPresenceDTO existingUser : existingUsers) {
                messagingTemplate.convertAndSendToUser(
                        user.getId().toString(),
                        "/queue/document/" + documentId + "/existing-users",
                        existingUser
                );
            }

            // Notify ALL users (including self) that new user joined
            messagingTemplate.convertAndSend(
                    "/topic/document/" + documentId + "/presence",
                    userPresence
            );

            log.info("User {} successfully joined document {}", userPresence.getUsername(), documentId);

        } catch (Exception e) {
            log.error("Error handling join: {}", e.getMessage(), e);
        }
    }
    /**
     * Handle document changes with Operational Transformation
     */
    @MessageMapping("/document/{documentId}/change")
    @SendTo("/topic/document/{documentId}/changes")
    public DocumentChangeDTO handleChange(
            @DestinationVariable String documentId,
            DocumentChangeDTO change) {

        log.info("Document change from user {} in document {}",
                change.getUsername(), documentId);

        try {
            CodeDocument document = documentService.getDocument(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            String newContent;
            Long newVersion;

            if (change.getOperation() != null) {
                // Incremental change
                TextOperation operation = change.getOperation();

                // Check version and transform if needed
                if (operation.getVersion() < document.getVersion()) {
                    log.warn("Operation version {} is behind document version {}",
                            operation.getVersion(), document.getVersion());
                    // In production, you'd transform against missed operations
                    // For now, we'll apply it anyway
                }

                // Apply operation
                newContent = otService.applyOperation(document.getContent(), operation);
                newVersion = document.getVersion() + 1;

            } else if (change.getContent() != null) {
                // Full content update (fallback)
                newContent = change.getContent();
                newVersion = document.getVersion() + 1;
            } else {
                log.warn("Received change with no operation or content");
                return change;
            }

            // Save updated document
            documentService.updateContent(documentId, newContent, newVersion);

            // Update and broadcast the change
            change.setVersion(newVersion);
            change.setTimestamp(System.currentTimeMillis());

            return change;

        } catch (Exception e) {
            log.error("Error handling change: {}", e.getMessage(), e);
            return change;
        }
    }

    /**
     * Handle cursor position updates
     */
    @MessageMapping("/document/{documentId}/cursor")
    @SendTo("/topic/document/{documentId}/cursors")
    public CursorPositionDTO handleCursor(
            @DestinationVariable String documentId,
            CursorPositionDTO cursor) {

        // Track cursor position
        documentCursors
                .computeIfAbsent(documentId, k -> new ConcurrentHashMap<>())
                .put(cursor.getUserId(), cursor);

        cursor.setTimestamp(System.currentTimeMillis());
        return cursor;
    }

    /**
     * Handle user leaving
     */
    @MessageMapping("/document/{documentId}/leave")
    @SendTo("/topic/document/{documentId}/presence")
    public UserPresenceDTO handleLeave(
            @DestinationVariable String documentId,
            UserPresenceDTO userPresence) {

        log.info("User {} leaving document {}", userPresence.getUsername(), documentId);

        try {
            // Remove user from document
            documentService.removeActiveUser(documentId, userPresence.getUserId());

            // Set user offline
            userService.setUserOnline(userPresence.getUsername(), false);

            // Remove cursor
            if (documentCursors.containsKey(documentId)) {
                documentCursors.get(documentId).remove(userPresence.getUserId());
            }

            userPresence.setOnline(false);
            return userPresence;

        } catch (Exception e) {
            log.error("Error handling leave: {}", e.getMessage(), e);
            return userPresence;
        }
    }
}