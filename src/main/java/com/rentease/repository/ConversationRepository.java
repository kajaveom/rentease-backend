package com.rentease.repository;

import com.rentease.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    // Find all conversations for a user
    @Query("SELECT c FROM Conversation c " +
            "WHERE c.participant1.id = :userId OR c.participant2.id = :userId " +
            "ORDER BY c.lastMessageAt DESC NULLS LAST")
    Page<Conversation> findByParticipant(@Param("userId") UUID userId, Pageable pageable);

    // Find conversation by id with participants loaded
    @Query("SELECT c FROM Conversation c " +
            "JOIN FETCH c.participant1 " +
            "JOIN FETCH c.participant2 " +
            "JOIN FETCH c.listing " +
            "WHERE c.id = :id")
    Optional<Conversation> findByIdWithDetails(@Param("id") UUID id);

    // Find existing conversation between two users for a listing
    @Query("SELECT c FROM Conversation c " +
            "WHERE c.listing.id = :listingId " +
            "AND ((c.participant1.id = :user1Id AND c.participant2.id = :user2Id) " +
            "OR (c.participant1.id = :user2Id AND c.participant2.id = :user1Id))")
    Optional<Conversation> findByListingAndParticipants(
            @Param("listingId") UUID listingId,
            @Param("user1Id") UUID user1Id,
            @Param("user2Id") UUID user2Id
    );

    // Count unread conversations for a user
    @Query("SELECT COUNT(DISTINCT c) FROM Conversation c " +
            "JOIN c.messages m " +
            "WHERE (c.participant1.id = :userId OR c.participant2.id = :userId) " +
            "AND m.sender.id != :userId " +
            "AND m.readAt IS NULL")
    long countUnreadConversations(@Param("userId") UUID userId);
}
