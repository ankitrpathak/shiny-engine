package com.carwash.adminservice.service;

import com.carwash.adminservice.dto.*;
import com.carwash.adminservice.security.CustomUserDetails;

import java.util.List;
import java.util.Map;

public interface AdminService {

    AdminDashboardStats getDashboardStats(String token);

    List<AdminActionResponse> getAuditLogs();

    List<UserDto> getAllUsers(String token);

    UserDto blockUser(Long id, String reason, CustomUserDetails admin, String token);

    UserDto activateUser(Long id, CustomUserDetails admin, String token);

    UserDto promoteToAdmin(Long id, CustomUserDetails admin, String token);

    UserDto promoteToWasher(Long id, CustomUserDetails admin, String token);

    UserDto demoteToCustomer(Long id, CustomUserDetails admin, String token);

    List<BookingDto> getAllBookings(String token);

    BookingDto assignWasher(Long bookingId, Long washerId, String washerName, CustomUserDetails admin, String token);

    List<Map<String, Object>> getAllPayments(String token);

    List<Map<String, Object>> getAllReviews();
}
