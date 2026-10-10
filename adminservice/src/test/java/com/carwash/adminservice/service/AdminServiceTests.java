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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTests {

    @Mock
    private AdminActionRepository adminActionRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private BookingClient bookingClient;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private ReviewClient reviewClient;

    @InjectMocks
    private AdminServiceImpl adminService;

    private CustomUserDetails adminUser;
    private CustomUserDetails godUser;

    @BeforeEach
    void setUp() {
        adminUser = new CustomUserDetails(1L, "admin@carwash.com", "ADMIN");
        godUser = new CustomUserDetails(99L, "god@carwash.com", "GOD");
    }

    @Test
    void testGetDashboardStats_AggregatesFromAllClients() {
        String token = "Bearer dummy_token";

        when(userClient.getUserSummary(token)).thenReturn(
                UserSummaryDto.builder()
                        .totalUsers(10L)
                        .totalCustomers(6L)
                        .totalWashers(3L)
                        .activeUsers(9L)
                        .inactiveUsers(1L)
                        .build());

        when(bookingClient.getBookingSummary(token)).thenReturn(
                BookingSummaryDto.builder()
                        .totalBookings(25L)
                        .completedBookings(18L)
                        .inProgressBookings(2L)
                        .build());

        when(paymentClient.getPaymentSummary(token)).thenReturn(
                PaymentSummaryDto.builder()
                        .totalRevenue(new BigDecimal("12500.00"))
                        .totalTransactions(20L)
                        .successfulTransactions(18L)
                        .build());

        when(reviewClient.getReviewSummary(token)).thenReturn(
                ReviewSummaryDto.builder()
                        .totalReviews(15L)
                        .averageRating(4.8)
                        .build());

        AdminDashboardStats stats = adminService.getDashboardStats(token);

        assertNotNull(stats);
        assertEquals(10L, stats.getTotalUsers());
        assertEquals(25L, stats.getTotalBookings());
        assertEquals(new BigDecimal("12500.00"), stats.getTotalRevenue());
        assertEquals(4.8, stats.getAverageRating());
    }

    @Test
    void testBlockUser_GodAccount_ThrowsBadRequestException() {
        String token = "Bearer dummy_token";
        UserDto godDto = UserDto.builder()
                .id(99L)
                .email("god@carwash.com")
                .role("GOD")
                .active(true)
                .build();

        when(userClient.getUserById(99L, token)).thenReturn(godDto);

        assertThrows(BadRequestException.class, () ->
                adminService.blockUser(99L, "Attempt to block GOD", adminUser, token));

        verify(userClient, never()).blockUser(any(), any());
    }

    @Test
    void testBlockUser_NormalUser_SuccessAndAuditLogged() {
        String token = "Bearer dummy_token";
        UserDto normalUser = UserDto.builder()
                .id(10L)
                .email("spammer@carwash.com")
                .role("CUSTOMER")
                .active(true)
                .build();

        UserDto blockedUser = UserDto.builder()
                .id(10L)
                .email("spammer@carwash.com")
                .role("CUSTOMER")
                .active(false)
                .build();

        when(userClient.getUserById(10L, token)).thenReturn(normalUser);
        when(userClient.blockUser(10L, token)).thenReturn(blockedUser);

        UserDto result = adminService.blockUser(10L, "Suspicious behavior", adminUser, token);

        assertNotNull(result);
        assertFalse(result.isActive());
        verify(adminActionRepository, times(1)).save(any(AdminAction.class));
    }

    @Test
    void testDemoteToCustomer_NonGodAdmin_ThrowsUnauthorizedException() {
        String token = "Bearer dummy_token";

        assertThrows(UnauthorizedException.class, () ->
                adminService.demoteToCustomer(5L, adminUser, token));

        verify(userClient, never()).demoteToCustomer(any(), any());
    }

    @Test
    void testAssignWasher_SuccessAndAuditLogged() {
        String token = "Bearer dummy_token";
        BookingDto bookingDto = BookingDto.builder()
                .id(101L)
                .washerId(5L)
                .washerName("Speedy Washer")
                .status("ASSIGNED")
                .build();

        when(bookingClient.assignWasher(101L, 5L, "Speedy Washer", token)).thenReturn(bookingDto);

        BookingDto result = adminService.assignWasher(101L, 5L, "Speedy Washer", adminUser, token);

        assertNotNull(result);
        assertEquals(5L, result.getWasherId());
        verify(adminActionRepository, times(1)).save(any(AdminAction.class));
    }

    @Test
    void testGetAuditLogs_ReturnsList() {
        AdminAction action = AdminAction.builder()
                .id(1L)
                .adminId(1L)
                .adminEmail("admin@carwash.com")
                .actionType("WASHER_ASSIGNED")
                .performedAt(LocalDateTime.now())
                .build();

        when(adminActionRepository.findAllByOrderByPerformedAtDesc()).thenReturn(List.of(action));

        List<AdminActionResponse> logs = adminService.getAuditLogs();

        assertEquals(1, logs.size());
        assertEquals("WASHER_ASSIGNED", logs.get(0).getActionType());
    }
}
