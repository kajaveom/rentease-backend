package com.rentease.controller;

import com.rentease.dto.request.SendMessageRequest;
import com.rentease.dto.response.MessageResponse;
import com.rentease.security.UserPrincipal;
import com.rentease.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle incoming chat messages via WebSocket
     * Client sends to: /app/chat/{conversationId}
     * Message is broadcast to: /topic/conversation/{conversationId}
     */
    @MessageMapping("/chat/{conversationId}")
    public void sendMessage(
            @DestinationVariable UUID conversationId,
            @Payload SendMessageRequest request,
            @AuthenticationPrincipal UserPrincipal user) {

        log.debug("WebSocket message received for conversation {} from user {}", conversationId, user.getId());

        // Save message using existing service
        MessageResponse response = messageService.sendMessage(user.getId(), conversationId, request);

        // Broadcast to all subscribers of this conversation
        messagingTemplate.convertAndSend(
            "/topic/conversation/" + conversationId,
            response
        );

        log.debug("Message broadcast to /topic/conversation/{}", conversationId);
    }

    /**
     * Handle typing indicator
     * Client sends to: /app/typing/{conversationId}
     * Broadcast to: /topic/conversation/{conversationId}/typing
     */
    @MessageMapping("/typing/{conversationId}")
    public void typingIndicator(
            @DestinationVariable UUID conversationId,
            @AuthenticationPrincipal UserPrincipal user) {

        messagingTemplate.convertAndSend(
            "/topic/conversation/" + conversationId + "/typing",
            new TypingNotification(user.getId(), user.getUsername())
        );
    }

    /**
     * Mark messages as read via WebSocket
     * Client sends to: /app/read/{conversationId}
     */
    @MessageMapping("/read/{conversationId}")
    public void markAsRead(
            @DestinationVariable UUID conversationId,
            @AuthenticationPrincipal UserPrincipal user) {

        messageService.markAsRead(user.getId(), conversationId);

        // Notify other user that messages were read
        messagingTemplate.convertAndSend(
            "/topic/conversation/" + conversationId + "/read",
            new ReadNotification(user.getId())
        );
    }

    // Simple DTOs for notifications
    public record TypingNotification(UUID userId, String firstName) {}
    public record ReadNotification(UUID userId) {}
}
