package com.rentease.dto.response;

import com.rentease.entity.Notification;
import com.rentease.entity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private NotificationType type;
    private String title;
    private String message;
    private String actionUrl;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    // Actor info (who triggered the notification)
    private ActorInfo actor;

    // Related entities (optional)
    private UUID relatedBookingId;
    private UUID relatedListingId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActorInfo {
        private UUID id;
        private String firstName;
        private String avatarUrl;
    }

    public static NotificationResponse fromEntity(Notification notification) {
        NotificationResponseBuilder builder = NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .actionUrl(notification.getActionUrl())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt());

        if (notification.getActor() != null) {
            builder.actor(ActorInfo.builder()
                    .id(notification.getActor().getId())
                    .firstName(notification.getActor().getFirstName())
                    .avatarUrl(notification.getActor().getAvatarUrl())
                    .build());
        }

        if (notification.getRelatedBooking() != null) {
            builder.relatedBookingId(notification.getRelatedBooking().getId());
        }

        if (notification.getRelatedListing() != null) {
            builder.relatedListingId(notification.getRelatedListing().getId());
        }

        return builder.build();
    }
}
