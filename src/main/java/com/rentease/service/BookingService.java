package com.rentease.service;

import com.rentease.dto.request.BookingActionRequest;
import com.rentease.dto.request.CreateBookingRequest;
import com.rentease.dto.response.BookingResponse;
import com.rentease.dto.response.PagedResponse;
import com.rentease.entity.Booking;
import com.rentease.entity.Listing;
import com.rentease.entity.User;
import com.rentease.entity.enums.BookingStatus;
import com.rentease.entity.enums.NotificationType;
import com.rentease.exception.BadRequestException;
import com.rentease.exception.ForbiddenException;
import com.rentease.exception.ResourceNotFoundException;
import com.rentease.repository.BookingRepository;
import com.rentease.repository.ListingRepository;
import com.rentease.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Transactional
    public BookingResponse createBooking(UUID renterId, UUID listingId, CreateBookingRequest request) {
        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        // Get listing
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        // Check if listing is available
        if (!listing.getAvailable() || !listing.getActive()) {
            throw new BadRequestException("This listing is not available for booking");
        }

        // Cannot book your own listing
        if (listing.getOwner().getId().equals(renterId)) {
            throw new BadRequestException("You cannot book your own listing");
        }

        // Check for overlapping bookings
        List<BookingStatus> activeStatuses = Arrays.asList(
                BookingStatus.APPROVED, BookingStatus.ACTIVE);
        if (bookingRepository.hasOverlappingBookings(listingId, request.getStartDate(), request.getEndDate(),
                activeStatuses)) {
            throw new BadRequestException("The selected dates overlap with an existing booking");
        }

        // Get renter
        User renter = userRepository.findById(renterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Calculate pricing (estimate only - no actual payment)
        int totalDays = (int) (request.getEndDate().toEpochDay() - request.getStartDate().toEpochDay()) + 1;
        int totalPrice = listing.getPricePerDay() * totalDays;

        // Create booking
        Booking booking = Booking.builder()
                .listing(listing)
                .renter(renter)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(totalDays)
                .dailyRate(listing.getPricePerDay())
                .totalPrice(totalPrice)
                .status(BookingStatus.REQUESTED)
                .renterMessage(request.getMessage())
                .build();

        Booking saved = bookingRepository.save(booking);

        // Notify the listing owner about the new booking request
        notificationService.createBookingNotification(
                NotificationType.BOOKING_REQUESTED,
                listing.getOwner(),
                renter,
                saved);

        // Send email notification
        emailService.sendBookingRequestEmail(saved.getId());

        return BookingResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(UUID userId, UUID bookingId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Only participants can view the booking
        if (!booking.isParticipant(userId)) {
            throw new ForbiddenException("You don't have access to this booking");
        }

        return BookingResponse.fromEntity(booking);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getMyBookings(UUID renterId, String status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Booking> bookings;

        if (status != null && !status.isEmpty()) {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            bookings = bookingRepository.findByRenterIdAndStatus(renterId, bookingStatus, pageRequest);
        } else {
            bookings = bookingRepository.findByRenterId(renterId, pageRequest);
        }

        return mapToPagedResponse(bookings);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getBookingRequests(UUID ownerId, String status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Booking> bookings;

        if (status != null && !status.isEmpty()) {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            bookings = bookingRepository.findByOwnerIdAndStatus(ownerId, bookingStatus, pageRequest);
        } else {
            bookings = bookingRepository.findByOwnerId(ownerId, pageRequest);
        }

        return mapToPagedResponse(bookings);
    }

    @Transactional
    public BookingResponse approveBooking(UUID ownerId, UUID bookingId, BookingActionRequest request) {
        Booking booking = getBookingForOwnerAction(ownerId, bookingId);

        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new BadRequestException("Only requested bookings can be approved");
        }

        booking.setStatus(BookingStatus.APPROVED);
        booking.setOwnerResponse(request.getResponse());
        booking.setApprovedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        // Notify the renter that their booking was approved
        notificationService.createBookingNotification(
                NotificationType.BOOKING_APPROVED,
                booking.getRenter(),
                booking.getListing().getOwner(),
                saved);

        // Send email notification
        emailService.sendBookingApprovedEmail(saved.getId());

        return BookingResponse.fromEntity(saved);
    }

    @Transactional
    public BookingResponse rejectBooking(UUID ownerId, UUID bookingId, BookingActionRequest request) {
        Booking booking = getBookingForOwnerAction(ownerId, bookingId);

        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new BadRequestException("Only requested bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setOwnerResponse(request.getResponse());

        Booking saved = bookingRepository.save(booking);

        // Notify the renter that their booking was rejected
        notificationService.createBookingNotification(
                NotificationType.BOOKING_REJECTED,
                booking.getRenter(),
                booking.getListing().getOwner(),
                saved);

        // Send email notification
        emailService.sendBookingDeclinedEmail(saved.getId());

        return BookingResponse.fromEntity(saved);
    }

    @Transactional
    public BookingResponse cancelBooking(UUID userId, UUID bookingId, BookingActionRequest request) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.isParticipant(userId)) {
            throw new ForbiddenException("You don't have access to this booking");
        }

        // Can only cancel bookings that aren't already completed or cancelled
        List<BookingStatus> cancellableStatuses = Arrays.asList(
                BookingStatus.REQUESTED, BookingStatus.APPROVED);
        if (!cancellableStatuses.contains(booking.getStatus())) {
            throw new BadRequestException("This booking cannot be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request.getCancellationReason());
        booking.setCancelledBy(userId);
        booking.setCancelledAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        // Notify the other party about the cancellation
        User canceller = userId.equals(booking.getRenter().getId()) ? booking.getRenter()
                : booking.getListing().getOwner();
        User recipient = userId.equals(booking.getRenter().getId()) ? booking.getListing().getOwner()
                : booking.getRenter();
        notificationService.createBookingNotification(
                NotificationType.BOOKING_CANCELLED,
                recipient,
                canceller,
                saved);

        // Send email notification
        emailService.sendBookingCancelledEmail(saved.getId(), userId);

        return BookingResponse.fromEntity(saved);
    }

    @Transactional
    public BookingResponse startBooking(UUID ownerId, UUID bookingId) {
        Booking booking = getBookingForOwnerAction(ownerId, bookingId);

        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new BadRequestException("Only approved bookings can be started");
        }

        booking.setStatus(BookingStatus.ACTIVE);
        booking.setStartedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        // Notify the renter that rental has started
        notificationService.createBookingNotification(
                NotificationType.BOOKING_STARTED,
                booking.getRenter(),
                booking.getListing().getOwner(),
                saved);

        return BookingResponse.fromEntity(saved);
    }

    @Transactional
    public BookingResponse completeBooking(UUID ownerId, UUID bookingId) {
        Booking booking = getBookingForOwnerAction(ownerId, bookingId);

        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new BadRequestException("Only active bookings can be completed");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        // Notify the renter that rental has been completed
        notificationService.createBookingNotification(
                NotificationType.BOOKING_COMPLETED,
                booking.getRenter(),
                booking.getListing().getOwner(),
                saved);

        // Send email notification prompting for a review
        emailService.sendBookingCompletedEmail(saved.getId());

        return BookingResponse.fromEntity(saved);
    }

    private Booking getBookingForOwnerAction(UUID ownerId, UUID bookingId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.isOwner(ownerId)) {
            throw new ForbiddenException("Only the listing owner can perform this action");
        }

        return booking;
    }

    private PagedResponse<BookingResponse> mapToPagedResponse(Page<Booking> page) {
        List<BookingResponse> content = page.getContent().stream()
                .map(BookingResponse::fromEntity)
                .toList();

        return PagedResponse.<BookingResponse>builder()
                .data(content)
                .pagination(PagedResponse.Pagination.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .build();
    }
}
