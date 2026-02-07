package com.rentease.repository;

import com.rentease.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Get paginated notifications for a user
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientId(@Param("userId") UUID userId, Pageable pageable);

    // Get unread notifications for a user
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId AND n.read = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByRecipientId(@Param("userId") UUID userId);

    // Count unread notifications
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :userId AND n.read = false")
    long countUnreadByRecipientId(@Param("userId") UUID userId);

    // Mark all notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipient.id = :userId AND n.read = false")
    int markAllAsReadForUser(@Param("userId") UUID userId);

    // Delete old read notifications (for cleanup, older than 30 days)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipient.id = :userId AND n.read = true AND n.createdAt < :cutoffDate")
    int deleteOldReadNotifications(@Param("userId") UUID userId, @Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
