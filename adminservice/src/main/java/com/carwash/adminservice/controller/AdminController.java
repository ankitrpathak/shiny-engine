package com.carwash.adminservice.controller;

import com.carwash.adminservice.dto.*;
import com.carwash.adminservice.security.CustomUserDetails;
import com.carwash.adminservice.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Operations", description = "Dashboard analytics, system audit logs, and administrative controls")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get aggregated platform dashboard KPI metrics")
    public ResponseEntity<AdminDashboardStats> getDashboardStats(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(adminService.getDashboardStats(token));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get admin operations audit logs")
    public ResponseEntity<List<AdminActionResponse>> getAuditLogs() {
        return ResponseEntity.ok(adminService.getAuditLogs());
    }

    @GetMapping("/users")
    @Operation(summary = "Get all platform users")
    public ResponseEntity<List<UserDto>> getAllUsers(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(adminService.getAllUsers(token));
    }

    @PutMapping("/users/{id}/block")
    @Operation(summary = "Block user account (God account is protected)")
    public ResponseEntity<UserDto> blockUser(
            @PathVariable Long id,
            @RequestBody(required = false) UpdateUserStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails admin,
            @RequestHeader("Authorization") String token) {
        String reason = (request != null && request.getReason() != null) ? request.getReason() : "Administrative block";
        return ResponseEntity.ok(adminService.blockUser(id, reason, admin, token));
    }

    @PutMapping("/users/{id}/activate")
    @Operation(summary = "Activate user account")
    public ResponseEntity<UserDto> activateUser(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails admin,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(adminService.activateUser(id, admin, token));
    }

    @PutMapping("/users/{id}/promote-admin")
    @Operation(summary = "Promote user to ADMIN")
    public ResponseEntity<UserDto> promoteToAdmin(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails admin,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(adminService.promoteToAdmin(id, admin, token));
    }

    @PutMapping("/users/{id}/promote-washer")
    @Operation(summary = "Promote user to WASHER")
    public ResponseEntity<UserDto> promoteToWasher(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails admin,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(adminService.promoteToWasher(id, admin, token));
    }

    @PutMapping("/users/{id}/demote-customer")
    @PreAuthorize("hasRole('GOD')")
    @Operation(summary = "Demote user to CUSTOMER (Super-admin GOD only)")
    public ResponseEntity<UserDto> demoteToCustomer(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails admin,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(adminService.demoteToCustomer(id, admin, token));
    }

    @GetMapping("/bookings")
    @Operation(summary = "Get all platform bookings")
    public ResponseEntity<List<BookingDto>> getAllBookings(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(adminService.getAllBookings(token));
    }

    @PutMapping("/bookings/{id}/assign")
    @Operation(summary = "Assign washer to booking")
    public ResponseEntity<BookingDto> assignWasher(
            @PathVariable Long id,
            @Valid @RequestBody AssignWasherRequest request,
            @AuthenticationPrincipal CustomUserDetails admin,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(adminService.assignWasher(id, request.getWasherId(), request.getWasherName(), admin, token));
    }

    @GetMapping("/payments")
    @Operation(summary = "Get all payments")
    public ResponseEntity<List<Map<String, Object>>> getAllPayments(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(adminService.getAllPayments(token));
    }

    @GetMapping("/reviews")
    @Operation(summary = "Get all customer reviews")
    public ResponseEntity<List<Map<String, Object>>> getAllReviews() {
        return ResponseEntity.ok(adminService.getAllReviews());
    }
}
