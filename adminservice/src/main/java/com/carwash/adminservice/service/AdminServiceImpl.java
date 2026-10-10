package com.carwash.adminservice.service;

import com.carwash.adminservice.client.BookingClient;
import com.carwash.adminservice.client.PaymentClient;
import com.carwash.adminservice.client.ReviewClient;
import com.carwash.adminservice.client.UserClient;
import com.carwash.adminservice.dto.*;
import com.carwash.adminservice.entity.AdminAction;
import com.carwash.adminservice.exception.BadRequestException;
import com.carwash.adminservice.exception.UnauthorizedException;
import com.carwash.adminservice.repository.AdminActionRepository;
import com.carwash.adminservice.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final AdminActionRepository adminActionRepository;
    private final UserClient userClient;
    private final BookingClient bookingClient;
    private final PaymentClient paymentClient;
    private final ReviewClient reviewClient;

    private static final String GOD_EMAIL = "god@carwash.com";

    @Override
    public AdminDashboardStats getDashboardStats(String token) {
        AdminDashboardStats.AdminDashboardStatsBuilder statsBuilder = AdminDashboardStats.builder();

        // 1. User metrics
        try {
            UserSummaryDto userSummary = userClient.getUserSummary(token);
            if (userSummary != null) {
                statsBuilder
                        .totalUsers(userSummary.getTotalUsers())
                        .totalCustomers(userSummary.getTotalCustomers())
                        .totalWashers(userSummary.getTotalWashers())
                        .activeUsers(userSummary.getActiveUsers())
                        .inactiveUsers(userSummary.getInactiveUsers());
            }
        } catch (Exception e) {
            log.warn("Could not retrieve user summary metrics: {}", e.getMessage());
        }

        // 2. Booking metrics
        try {
            BookingSummaryDto bookingSummary = bookingClient.getBookingSummary(token);
            if (bookingSummary != null) {
                statsBuilder
                        .totalBookings(bookingSummary.getTotalBookings())
                        .pendingBookings(bookingSummary.getPendingBookings())
                        .confirmedBookings(bookingSummary.getConfirmedBookings())
                        .assignedBookings(bookingSummary.getAssignedBookings())
                        .inProgressBookings(bookingSummary.getInProgressBookings())
                        .completedBookings(bookingSummary.getCompletedBookings())
                        .cancelledBookings(bookingSummary.getCancelledBookings());
            }
        } catch (Exception e) {
            log.warn("Could not retrieve booking summary metrics: {}", e.getMessage());
        }

        // 3. Payment metrics
        try {
            PaymentSummaryDto paymentSummary = paymentClient.getPaymentSummary(token);
            if (paymentSummary != null) {
                statsBuilder
                        .totalRevenue(paymentSummary.getTotalRevenue() != null ? paymentSummary.getTotalRevenue() : BigDecimal.ZERO)
                        .totalPaymentsCount(paymentSummary.getTotalTransactions())
                        .successfulPaymentsCount(paymentSummary.getSuccessfulTransactions());
            } else {
                statsBuilder.totalRevenue(BigDecimal.ZERO);
            }
        } catch (Exception e) {
            log.warn("Could not retrieve payment summary metrics: {}", e.getMessage());
            statsBuilder.totalRevenue(BigDecimal.ZERO);
        }

        // 4. Review metrics
        try {
            ReviewSummaryDto reviewSummary = reviewClient.getReviewSummary(token);
            if (reviewSummary != null) {
                statsBuilder
                        .totalReviews(reviewSummary.getTotalReviews())
                        .averageRating(reviewSummary.getAverageRating());
            }
        } catch (Exception e) {
            log.warn("Could not retrieve review summary metrics: {}", e.getMessage());
        }

        return statsBuilder.build();
    }

    @Override
    public List<AdminActionResponse> getAuditLogs() {
        return adminActionRepository.findAllByOrderByPerformedAtDesc()
                .stream()
                .map(this::mapToActionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getAllUsers(String token) {
        return userClient.getAllUsers(token);
    }

    @Override
    @Transactional
    public UserDto blockUser(Long id, String reason, CustomUserDetails admin, String token) {
        UserDto targetUser = userClient.getUserById(id, token);
        if (targetUser != null && (GOD_EMAIL.equalsIgnoreCase(targetUser.getEmail()) || "GOD".equalsIgnoreCase(targetUser.getRole()))) {
            throw new BadRequestException("Root super-admin (GOD) account is immutable and cannot be blocked.");
        }

        UserDto updated = userClient.blockUser(id, token);
        logAction(admin.getUserId(), admin.getEmail(), "USER_BLOCKED", "USER", id, "Blocked reason: " + (reason != null ? reason : "Administrative block"));
        return updated;
    }

    @Override
    @Transactional
    public UserDto activateUser(Long id, CustomUserDetails admin, String token) {
        UserDto updated = userClient.activateUser(id, token);
        logAction(admin.getUserId(), admin.getEmail(), "USER_ACTIVATED", "USER", id, "User reactivated");
        return updated;
    }

    @Override
    @Transactional
    public UserDto promoteToAdmin(Long id, CustomUserDetails admin, String token) {
        UserDto updated = userClient.promoteToAdmin(id, token);
        logAction(admin.getUserId(), admin.getEmail(), "USER_PROMOTED_ADMIN", "USER", id, "Promoted to ADMIN role");
        return updated;
    }

    @Override
    @Transactional
    public UserDto promoteToWasher(Long id, CustomUserDetails admin, String token) {
        UserDto updated = userClient.promoteToWasher(id, token);
        logAction(admin.getUserId(), admin.getEmail(), "USER_PROMOTED_WASHER", "USER", id, "Promoted to WASHER role");
        return updated;
    }

    @Override
    @Transactional
    public UserDto demoteToCustomer(Long id, CustomUserDetails admin, String token) {
        if (!"GOD".equalsIgnoreCase(admin.getRole())) {
            throw new UnauthorizedException("Only super-admin (GOD) can demote accounts.");
        }

        UserDto targetUser = userClient.getUserById(id, token);
        if (targetUser != null && (GOD_EMAIL.equalsIgnoreCase(targetUser.getEmail()) || "GOD".equalsIgnoreCase(targetUser.getRole()))) {
            throw new BadRequestException("Root super-admin (GOD) account cannot be demoted.");
        }

        UserDto updated = userClient.demoteToCustomer(id, token);
        logAction(admin.getUserId(), admin.getEmail(), "USER_DEMOTED_CUSTOMER", "USER", id, "Demoted to CUSTOMER role");
        return updated;
    }

    @Override
    public List<BookingDto> getAllBookings(String token) {
        return bookingClient.getAllBookings(token);
    }

    @Override
    @Transactional
    public BookingDto assignWasher(Long bookingId, Long washerId, String washerName, CustomUserDetails admin, String token) {
        BookingDto updated = bookingClient.assignWasher(bookingId, washerId, washerName, token);
        logAction(admin.getUserId(), admin.getEmail(), "WASHER_ASSIGNED", "BOOKING", bookingId,
                String.format("Assigned washer #%d (%s) to booking #%d", washerId, washerName, bookingId));
        return updated;
    }

    @Override
    public List<Map<String, Object>> getAllPayments(String token) {
        try {
            return paymentClient.getAllPayments(token);
        } catch (Exception e) {
            log.warn("Could not retrieve payments: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Map<String, Object>> getAllReviews() {
        try {
            return reviewClient.getAllReviews();
        } catch (Exception e) {
            log.warn("Could not retrieve reviews: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private void logAction(Long adminId, String adminEmail, String actionType, String targetResource, Long targetId, String details) {
        try {
            AdminAction action = AdminAction.builder()
                    .adminId(adminId != null ? adminId : 0L)
                    .adminEmail(adminEmail != null ? adminEmail : "admin@carwash.com")
                    .actionType(actionType)
                    .targetResource(targetResource)
                    .targetId(targetId)
                    .details(details)
                    .build();
            adminActionRepository.save(action);
            log.info("Admin action logged: {} by {} on {}:{}", actionType, adminEmail, targetResource, targetId);
        } catch (Exception e) {
            log.error("Failed to log admin action: {}", e.getMessage(), e);
        }
    }

    private AdminActionResponse mapToActionResponse(AdminAction a) {
        return AdminActionResponse.builder()
                .id(a.getId())
                .adminId(a.getAdminId())
                .adminEmail(a.getAdminEmail())
                .actionType(a.getActionType())
                .targetResource(a.getTargetResource())
                .targetId(a.getTargetId())
                .details(a.getDetails())
                .performedAt(a.getPerformedAt())
                .build();
    }
}
