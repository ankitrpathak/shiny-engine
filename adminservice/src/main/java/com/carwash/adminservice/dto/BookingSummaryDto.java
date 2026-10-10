package com.carwash.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSummaryDto {
    private long totalBookings;
    private long pendingBookings;
    private long confirmedBookings;
    private long assignedBookings;
    private long inProgressBookings;
    private long completedBookings;
    private long cancelledBookings;
}
