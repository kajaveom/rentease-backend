package com.rentease.dto.response;

import com.rentease.entity.Message;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MessageResponse {
    private UUID id;
    private UUID conversationId;
    private SenderInfo sender;
    private String content;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class SenderInfo {
        private UUID id;
        private String firstName;
        private String lastName;
        private String avatarUrl;
    }

    public static MessageResponse fromEntity(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .sender(SenderInfo.builder()
                        .id(message.getSender().getId())
                        .firstName(message.getSender().getFirstName())
                        .lastName(message.getSender().getLastName())
                        .avatarUrl(message.getSender().getAvatarUrl())
                        .build())
                .content(message.getContent())
                .isRead(message.isRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
