package com.carwash.adminservice.client;

import com.carwash.adminservice.dto.BookingDto;
import com.carwash.adminservice.dto.BookingSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "booking-service")
public interface BookingClient {

    @GetMapping("/api/bookings")
    List<BookingDto> getAllBookings(@RequestHeader("Authorization") String token);

    @GetMapping("/api/bookings/{bookingId}")
    BookingDto getBookingById(@PathVariable("bookingId") Long bookingId, @RequestHeader("Authorization") String token);

    @PutMapping("/api/bookings/{bookingId}/assign/{washerId}")
    BookingDto assignWasher(
            @PathVariable("bookingId") Long bookingId,
            @PathVariable("washerId") Long washerId,
            @RequestParam("washerName") String washerName,
            @RequestHeader("Authorization") String token);

    @GetMapping("/api/bookings/admin/summary")
    BookingSummaryDto getBookingSummary(@RequestHeader("Authorization") String token);
}
