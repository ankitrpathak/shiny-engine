package com.carwash.paymentservice.client;

import com.carwash.paymentservice.dto.BookingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "booking-service")
public interface BookingClient {

    @GetMapping("/api/bookings/{bookingId}")
    BookingDto getBookingById(@PathVariable("bookingId") Long bookingId);

    @PutMapping("/api/bookings/{bookingId}/paid")
    BookingDto markBookingPaid(@PathVariable("bookingId") Long bookingId);
}
