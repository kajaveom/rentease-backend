package com.rentease.dto.response;

import com.rentease.entity.Conversation;
import com.rentease.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ConversationResponse {
    private UUID id;
    private ListingInfo listing;
    private ParticipantInfo otherParticipant;
    private UUID bookingId;
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class ListingInfo {
        private UUID id;
        private String title;
        private String primaryImage;
    }

    @Data
    @Builder
    public static class ParticipantInfo {
        private UUID id;
        private String firstName;
        private String lastName;
        private String avatarUrl;
        private Boolean idVerified;
    }

    public static ConversationResponse fromEntity(Conversation conversation, UUID currentUserId, Long unreadCount) {
        User otherUser = conversation.getOtherParticipant(currentUserId);

        String primaryImage = null;
        if (conversation.getListing().getImages() != null && !conversation.getListing().getImages().isEmpty()) {
            primaryImage = conversation.getListing().getImages().get(0).getImageUrl();
        }

        return ConversationResponse.builder()
                .id(conversation.getId())
                .listing(ListingInfo.builder()
                        .id(conversation.getListing().getId())
                        .title(conversation.getListing().getTitle())
                        .primaryImage(primaryImage)
                        .build())
                .otherParticipant(ParticipantInfo.builder()
                        .id(otherUser.getId())
                        .firstName(otherUser.getFirstName())
                        .lastName(otherUser.getLastName())
                        .avatarUrl(otherUser.getAvatarUrl())
                        .idVerified(otherUser.getIdVerified())
                        .build())
                .bookingId(conversation.getBooking() != null ? conversation.getBooking().getId() : null)
                .lastMessagePreview(conversation.getLastMessagePreview())
                .lastMessageAt(conversation.getLastMessageAt())
                .unreadCount(unreadCount)
                .createdAt(conversation.getCreatedAt())
                .build();
    }
}
