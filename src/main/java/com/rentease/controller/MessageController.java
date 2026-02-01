package com.rentease.controller;

import com.rentease.dto.request.SendMessageRequest;
import com.rentease.dto.response.ApiResponse;
import com.rentease.dto.response.ConversationResponse;
import com.rentease.dto.response.MessageResponse;
import com.rentease.dto.response.PagedResponse;
import com.rentease.security.CurrentUser;
import com.rentease.security.UserPrincipal;
import com.rentease.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // Get all conversations for current user
    @GetMapping("/conversations")
    public ResponseEntity<PagedResponse<ConversationResponse>> getConversations(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PagedResponse<ConversationResponse> response = messageService.getConversations(
                currentUser.getId(), page, size
        );
        return ResponseEntity.ok(response);
    }

    // Get a specific conversation
    @GetMapping("/conversations/{id}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        ConversationResponse response = messageService.getConversation(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Get messages in a conversation
    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<PagedResponse<MessageResponse>> getMessages(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        PagedResponse<MessageResponse> response = messageService.getMessages(
                currentUser.getId(), id, page, size
        );
        return ResponseEntity.ok(response);
    }

    // Send a message to an existing conversation
    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody SendMessageRequest request
    ) {
        MessageResponse response = messageService.sendMessage(currentUser.getId(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // Start a new conversation with a user about a listing
    @PostMapping("/users/{recipientId}/conversations")
    public ResponseEntity<ApiResponse<ConversationResponse>> startConversation(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID recipientId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        if (request.getListingId() == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("BAD_REQUEST", "Listing ID is required to start a conversation")
            );
        }

        ConversationResponse response = messageService.startConversation(
                currentUser.getId(),
                recipientId,
                request.getListingId(),
                request.getContent()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // Mark messages in a conversation as read
    @PostMapping("/conversations/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        messageService.markAsRead(currentUser.getId(), id);
        return ResponseEntity.ok().build();
    }

    // Get unread message count
    @GetMapping("/messages/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @CurrentUser UserPrincipal currentUser
    ) {
        long count = messageService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }
}
