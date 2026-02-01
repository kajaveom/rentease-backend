package com.rentease.service;

import com.rentease.dto.request.SendMessageRequest;
import com.rentease.dto.response.ConversationResponse;
import com.rentease.dto.response.MessageResponse;
import com.rentease.dto.response.PagedResponse;
import com.rentease.entity.Conversation;
import com.rentease.entity.Listing;
import com.rentease.entity.Message;
import com.rentease.entity.User;
import com.rentease.exception.BadRequestException;
import com.rentease.exception.ForbiddenException;
import com.rentease.exception.ResourceNotFoundException;
import com.rentease.repository.ConversationRepository;
import com.rentease.repository.ListingRepository;
import com.rentease.repository.MessageRepository;
import com.rentease.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    @Transactional(readOnly = true)
    public PagedResponse<ConversationResponse> getConversations(UUID userId, int page, int size) {
        Page<Conversation> conversations = conversationRepository.findByParticipant(
                userId, PageRequest.of(page, size)
        );

        List<ConversationResponse> content = conversations.getContent().stream()
                .map(conv -> {
                    long unread = messageRepository.countUnreadInConversation(conv.getId(), userId);
                    return ConversationResponse.fromEntity(conv, userId, unread);
                })
                .toList();

        return PagedResponse.<ConversationResponse>builder()
                .data(content)
                .pagination(PagedResponse.PaginationInfo.builder()
                        .page(conversations.getNumber())
                        .size(conversations.getSize())
                        .totalElements(conversations.getTotalElements())
                        .totalPages(conversations.getTotalPages())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(UUID userId, UUID conversationId) {
        Conversation conversation = conversationRepository.findByIdWithDetails(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.isParticipant(userId)) {
            throw new ForbiddenException("You don't have access to this conversation");
        }

        long unread = messageRepository.countUnreadInConversation(conversationId, userId);
        return ConversationResponse.fromEntity(conversation, userId, unread);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MessageResponse> getMessages(UUID userId, UUID conversationId, int page, int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.isParticipant(userId)) {
            throw new ForbiddenException("You don't have access to this conversation");
        }

        Page<Message> messages = messageRepository.findByConversationId(
                conversationId, PageRequest.of(page, size)
        );

        List<MessageResponse> content = messages.getContent().stream()
                .map(MessageResponse::fromEntity)
                .toList();

        return PagedResponse.<MessageResponse>builder()
                .data(content)
                .pagination(PagedResponse.PaginationInfo.builder()
                        .page(messages.getNumber())
                        .size(messages.getSize())
                        .totalElements(messages.getTotalElements())
                        .totalPages(messages.getTotalPages())
                        .build())
                .build();
    }

    @Transactional
    public MessageResponse sendMessage(UUID senderId, UUID conversationId, SendMessageRequest request) {
        Conversation conversation = conversationRepository.findByIdWithDetails(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.isParticipant(senderId)) {
            throw new ForbiddenException("You don't have access to this conversation");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent())
                .build();

        Message saved = messageRepository.save(message);
        return MessageResponse.fromEntity(saved);
    }

    @Transactional
    public ConversationResponse startConversation(UUID senderId, UUID recipientId, UUID listingId, String initialMessage) {
        // Validate users are different
        if (senderId.equals(recipientId)) {
            throw new BadRequestException("Cannot start a conversation with yourself");
        }

        // Get the listing
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        // Check if conversation already exists
        var existingConversation = conversationRepository.findByListingAndParticipants(
                listingId, senderId, recipientId
        );

        if (existingConversation.isPresent()) {
            // Add message to existing conversation
            Conversation conversation = existingConversation.get();
            User sender = userRepository.findById(senderId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Message message = Message.builder()
                    .conversation(conversation)
                    .sender(sender)
                    .content(initialMessage)
                    .build();
            messageRepository.save(message);

            return ConversationResponse.fromEntity(conversation, senderId, 0L);
        }

        // Create new conversation
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        Conversation conversation = Conversation.builder()
                .listing(listing)
                .participant1(listing.getOwner().getId().equals(senderId) ? sender : recipient)
                .participant2(listing.getOwner().getId().equals(senderId) ? recipient : sender)
                .build();

        Conversation savedConversation = conversationRepository.save(conversation);

        // Create initial message
        Message message = Message.builder()
                .conversation(savedConversation)
                .sender(sender)
                .content(initialMessage)
                .build();
        messageRepository.save(message);

        return ConversationResponse.fromEntity(savedConversation, senderId, 0L);
    }

    @Transactional
    public void markAsRead(UUID userId, UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.isParticipant(userId)) {
            throw new ForbiddenException("You don't have access to this conversation");
        }

        messageRepository.markAsRead(conversationId, userId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return messageRepository.countUnreadMessages(userId);
    }
}
