package com.carwash.reviewservice.client;

import com.carwash.reviewservice.dto.BookingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "booking-service")
public interface BookingClient {

    @GetMapping("/api/bookings/{bookingId}")
    BookingDto getBookingById(@PathVariable("bookingId") Long bookingId);
}
