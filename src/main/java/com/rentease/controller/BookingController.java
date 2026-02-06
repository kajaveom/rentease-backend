package com.rentease.controller;

import com.rentease.dto.request.BookingActionRequest;
import com.rentease.dto.request.CreateBookingRequest;
import com.rentease.dto.response.ApiResponse;
import com.rentease.dto.response.BookingResponse;
import com.rentease.dto.response.PagedResponse;
import com.rentease.security.CurrentUser;
import com.rentease.security.UserPrincipal;
import com.rentease.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingController {

        private final BookingService bookingService;

        @PostMapping("/listings/{listingId}/bookings")
        public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
                        @CurrentUser UserPrincipal currentUser,
                        @PathVariable("listingId") UUID listingId,
                        @Valid @RequestBody CreateBookingRequest request) {
                BookingResponse response = bookingService.createBooking(currentUser.getId(), listingId, request);
                return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
        }

        @GetMapping("/bookings/{id}")
        public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
                        @CurrentUser UserPrincipal currentUser,
                        @PathVariable("id") UUID id) {
                BookingResponse response = bookingService.getBooking(currentUser.getId(), id);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        // Get bookings where I am the renter
        @GetMapping("/users/me/bookings")
        public ResponseEntity<PagedResponse<BookingResponse>> getMyBookings(
                        @CurrentUser UserPrincipal currentUser,
                        @RequestParam(value = "status", required = false) String status,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "20") int size) {
                PagedResponse<BookingResponse> response = bookingService.getMyBookings(
                                currentUser.getId(), status, page, size);
                return ResponseEntity.ok(response);
        }

        // Get booking requests for my listings (as owner)
        @GetMapping("/users/me/booking-requests")
        public ResponseEntity<PagedResponse<BookingResponse>> getBookingRequests(
                        @CurrentUser UserPrincipal currentUser,
                        @RequestParam(value = "status", required = false) String status,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "20") int size) {
                PagedResponse<BookingResponse> response = bookingService.getBookingRequests(
                                currentUser.getId(), status, page, size);
                return ResponseEntity.ok(response);
        }

        @PostMapping("/bookings/{id}/approve")
        public ResponseEntity<ApiResponse<BookingResponse>> approveBooking(
                        @CurrentUser UserPrincipal currentUser,
                        @PathVariable("id") UUID id,
                        @Valid @RequestBody(required = false) BookingActionRequest request) {
                BookingResponse response = bookingService.approveBooking(
                                currentUser.getId(), id, request != null ? request : new BookingActionRequest());
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @PostMapping("/bookings/{id}/reject")
        public ResponseEntity<ApiResponse<BookingResponse>> rejectBooking(
                        @CurrentUser UserPrincipal currentUser,
                        @PathVariable("id") UUID id,
                        @Valid @RequestBody(required = false) BookingActionRequest request) {
                BookingResponse response = bookingService.rejectBooking(
                                currentUser.getId(), id, request != null ? request : new BookingActionRequest());
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @PostMapping("/bookings/{id}/cancel")
        public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
                        @CurrentUser UserPrincipal currentUser,
                        @PathVariable("id") UUID id,
                        @Valid @RequestBody(required = false) BookingActionRequest request) {
                BookingResponse response = bookingService.cancelBooking(
                                currentUser.getId(), id, request != null ? request : new BookingActionRequest());
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @PostMapping("/bookings/{id}/start")
        public ResponseEntity<ApiResponse<BookingResponse>> startBooking(
                        @CurrentUser UserPrincipal currentUser,
                        @PathVariable("id") UUID id) {
                BookingResponse response = bookingService.startBooking(currentUser.getId(), id);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @PostMapping("/bookings/{id}/complete")
        public ResponseEntity<ApiResponse<BookingResponse>> completeBooking(
                        @CurrentUser UserPrincipal currentUser,
                        @PathVariable("id") UUID id) {
                BookingResponse response = bookingService.completeBooking(currentUser.getId(), id);
                return ResponseEntity.ok(ApiResponse.success(response));
        }
}
