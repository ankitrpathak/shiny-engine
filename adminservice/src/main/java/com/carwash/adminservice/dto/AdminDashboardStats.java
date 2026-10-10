package com.carwash.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStats {
    // User metrics
    private long totalUsers;
    private long totalCustomers;
    private long totalWashers;
    private long activeUsers;
    private long inactiveUsers;

    // Booking metrics
    private long totalBookings;
    private long pendingBookings;
    private long confirmedBookings;
    private long assignedBookings;
    private long inProgressBookings;
    private long completedBookings;
    private long cancelledBookings;

    // Financial metrics
    private BigDecimal totalRevenue;
    private long totalPaymentsCount;
    private long successfulPaymentsCount;

    // Review metrics
    private long totalReviews;
    private double averageRating;
}
