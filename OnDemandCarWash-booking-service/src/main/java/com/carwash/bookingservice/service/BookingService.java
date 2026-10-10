package com.carwash.bookingservice.service;

import com.carwash.bookingservice.dto.BookingResponse;
import com.carwash.bookingservice.dto.BookingSummaryResponse;
import com.carwash.bookingservice.dto.CreateBookingRequest;
import com.carwash.bookingservice.entity.BookingStatus;
import com.carwash.bookingservice.security.CustomUserDetails;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(CreateBookingRequest request, CustomUserDetails currentUser);
    BookingResponse getBookingById(Long bookingId, CustomUserDetails currentUser);
    List<BookingResponse> getMyBookings(CustomUserDetails currentUser);
    List<BookingResponse> getMyAssignments(CustomUserDetails currentUser);
    List<BookingResponse> getAllBookings();
    BookingResponse assignWasher(Long bookingId, Long washerId, String washerName);
    BookingResponse updateBookingStatus(Long bookingId, BookingStatus status, CustomUserDetails currentUser);
    BookingResponse markPaid(Long bookingId);
    BookingSummaryResponse getBookingSummary();
}
