package com.rentease.repository;

import com.rentease.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    // Find messages for a conversation (paginated, newest first for loading more)
    @Query("SELECT m FROM Message m " +
            "JOIN FETCH m.sender " +
            "WHERE m.conversation.id = :conversationId " +
            "ORDER BY m.createdAt DESC")
    Page<Message> findByConversationId(@Param("conversationId") UUID conversationId, Pageable pageable);

    // Mark messages as read
    @Modifying
    @Query("UPDATE Message m SET m.readAt = :readAt " +
            "WHERE m.conversation.id = :conversationId " +
            "AND m.sender.id != :userId " +
            "AND m.readAt IS NULL")
    int markAsRead(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId,
            @Param("readAt") LocalDateTime readAt
    );

    // Count unread messages for a user
    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE (m.conversation.participant1.id = :userId OR m.conversation.participant2.id = :userId) " +
            "AND m.sender.id != :userId " +
            "AND m.readAt IS NULL")
    long countUnreadMessages(@Param("userId") UUID userId);

    // Count unread messages in a conversation for a user
    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE m.conversation.id = :conversationId " +
            "AND m.sender.id != :userId " +
            "AND m.readAt IS NULL")
    long countUnreadInConversation(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId
    );
}
