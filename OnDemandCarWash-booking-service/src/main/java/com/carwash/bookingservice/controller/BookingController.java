package com.carwash.bookingservice.controller;

import com.carwash.bookingservice.dto.BookingResponse;
import com.carwash.bookingservice.dto.BookingSummaryResponse;
import com.carwash.bookingservice.dto.CreateBookingRequest;
import com.carwash.bookingservice.entity.BookingStatus;
import com.carwash.bookingservice.security.CustomUserDetails;
import com.carwash.bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Car Wash Booking Lifecycle and Assignment Endpoints")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Create a new booking (Customer)")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return new ResponseEntity<>(bookingService.createBooking(request, currentUser), HttpStatus.CREATED);
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking details by ID (Owner, Assigned Washer, or Admin)")
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId, currentUser));
    }

    @GetMapping("/my-bookings")
    @Operation(summary = "Get bookings of currently logged in customer")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(bookingService.getMyBookings(currentUser));
    }

    @GetMapping("/my-assignments")
    @PreAuthorize("hasAnyRole('WASHER', 'ADMIN', 'GOD')")
    @Operation(summary = "Get bookings assigned to currently logged in washer")
    public ResponseEntity<List<BookingResponse>> getMyAssignments(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(bookingService.getMyAssignments(currentUser));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Get all platform bookings (Admin only)")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @PutMapping("/{bookingId}/assign/{washerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Assign washer to booking (Admin only)")
    public ResponseEntity<BookingResponse> assignWasher(
            @PathVariable Long bookingId,
            @PathVariable Long washerId,
            @RequestParam(required = false, defaultValue = "Assigned Washer") String washerName) {
        return ResponseEntity.ok(bookingService.assignWasher(bookingId, washerId, washerName));
    }

    @PutMapping("/{bookingId}/status/{status}")
    @Operation(summary = "Update booking status (Allowed transitions enforced per role)")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @PathVariable Long bookingId,
            @PathVariable BookingStatus status,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(bookingId, status, currentUser));
    }

    @PutMapping("/{bookingId}/paid")
    @Operation(summary = "Mark booking as paid (Internal or Admin)")
    public ResponseEntity<BookingResponse> markPaid(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.markPaid(bookingId));
    }

    @GetMapping("/admin/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Get booking analytics and counts for admin dashboard")
    public ResponseEntity<BookingSummaryResponse> getBookingSummary() {
        return ResponseEntity.ok(bookingService.getBookingSummary());
    }
}
