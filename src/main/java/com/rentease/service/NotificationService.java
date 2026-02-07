package com.rentease.service;

import com.rentease.dto.response.NotificationResponse;
import com.rentease.dto.response.PagedResponse;
import com.rentease.entity.Booking;
import com.rentease.entity.Listing;
import com.rentease.entity.Notification;
import com.rentease.entity.User;
import com.rentease.entity.enums.NotificationType;
import com.rentease.exception.ResourceNotFoundException;
import com.rentease.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createBookingNotification(NotificationType type, User recipient, User actor, Booking booking) {
        String title;
        String message;
        String actionUrl = "/bookings/" + booking.getId();

        switch (type) {
            case BOOKING_REQUESTED -> {
                title = "New Booking Request";
                message = actor.getFirstName() + " wants to rent your \"" + booking.getListing().getTitle() + "\"";
            }
            case BOOKING_APPROVED -> {
                title = "Booking Approved";
                message = "Your booking for \"" + booking.getListing().getTitle() + "\" has been approved!";
            }
            case BOOKING_REJECTED -> {
                title = "Booking Declined";
                message = "Your booking for \"" + booking.getListing().getTitle() + "\" was declined.";
            }
            case BOOKING_CANCELLED -> {
                title = "Booking Cancelled";
                message = "The booking for \"" + booking.getListing().getTitle() + "\" has been cancelled.";
            }
            case BOOKING_STARTED -> {
                title = "Rental Started";
                message = "Your rental of \"" + booking.getListing().getTitle() + "\" has started.";
            }
            case BOOKING_COMPLETED -> {
                title = "Rental Completed";
                message = "The rental of \"" + booking.getListing().getTitle() + "\" has been completed.";
            }
            default -> {
                title = "Notification";
                message = "You have a new notification.";
            }
        }

        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .title(title)
                .message(message)
                .actionUrl(actionUrl)
                .relatedBooking(booking)
                .relatedListing(booking.getListing())
                .build();

        notificationRepository.save(notification);
        log.info("Created {} notification for user {}", type, recipient.getId());
    }

    @Transactional
    public void createReviewNotification(User recipient, User reviewer, Listing listing) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(reviewer)
                .type(NotificationType.REVIEW_RECEIVED)
                .title("New Review")
                .message(reviewer.getFirstName() + " left a review on \"" + listing.getTitle() + "\"")
                .actionUrl("/listings/" + listing.getId())
                .relatedListing(listing)
                .build();

        notificationRepository.save(notification);
        log.info("Created REVIEW_RECEIVED notification for user {}", recipient.getId());
    }

    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getNotifications(UUID userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByRecipientId(userId, pageRequest);

        List<NotificationResponse> content = notifications.getContent().stream()
                .map(NotificationResponse::fromEntity)
                .toList();

        return PagedResponse.<NotificationResponse>builder()
                .data(content)
                .pagination(PagedResponse.Pagination.builder()
                        .page(notifications.getNumber())
                        .size(notifications.getSize())
                        .totalElements(notifications.getTotalElements())
                        .totalPages(notifications.getTotalPages())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(UUID userId) {
        return notificationRepository.findUnreadByRecipientId(userId).stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countUnreadByRecipientId(userId);
    }

    @Transactional
    public void markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification", "id", notificationId);
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Transactional
    public int markAllAsRead(UUID userId) {
        return notificationRepository.markAllAsReadForUser(userId);
    }
}
