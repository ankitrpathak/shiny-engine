package com.carwash.bookingservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSummaryResponse {
    private long totalBookings;
    private long pendingBookings;
    private long assignedBookings;
    private long inProgressBookings;
    private long completedBookings;
    private long cancelledBookings;
    private BigDecimal totalRevenue;
}
