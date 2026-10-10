package com.carwash.userservice.controller;

import com.carwash.userservice.dto.UpdateProfileRequest;
import com.carwash.userservice.dto.UserResponse;
import com.carwash.userservice.dto.UserSummaryResponse;
import com.carwash.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile, role management, and administration")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getCurrentUser(userDetails.getUsername()));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current profile (alias for /me)")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getCurrentUser(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD') or principal.username == @userServiceImpl.getUserById(#id).email")
    @Operation(summary = "Get user by ID (Admin or self)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "List all users (Admin only)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/washers")
    @Operation(summary = "List active washers")
    public ResponseEntity<List<UserResponse>> getActiveWashers() {
        return ResponseEntity.ok(userService.getActiveWashers());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile (Owner or Admin)")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.updateUser(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Delete user (Admin only, God account is protected)")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/block")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Block user (Admin only, God account cannot be blocked)")
    public ResponseEntity<UserResponse> blockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.blockUser(id));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Activate user (Admin only)")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.activateUser(id));
    }

    @PutMapping("/{id}/promote-admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Promote user to ADMIN (Admin or God)")
    public ResponseEntity<UserResponse> promoteToAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(userService.promoteToAdmin(id));
    }

    @PutMapping("/{id}/promote-washer")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Promote user to WASHER (Admin or God)")
    public ResponseEntity<UserResponse> promoteToWasher(@PathVariable Long id) {
        return ResponseEntity.ok(userService.promoteToWasher(id));
    }

    @PutMapping("/{id}/demote-customer")
    @PreAuthorize("hasRole('GOD')")
    @Operation(summary = "Demote user to CUSTOMER (God Super Admin only)")
    public ResponseEntity<UserResponse> demoteToCustomer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.demoteToCustomer(id, userDetails.getUsername()));
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Get total count of users")
    public ResponseEntity<Long> getCount() {
        return ResponseEntity.ok(userService.countTotal());
    }

    @GetMapping("/count/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Get count of active users")
    public ResponseEntity<Long> getActiveCount() {
        return ResponseEntity.ok(userService.countActive());
    }

    @GetMapping("/count/blocked")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Get count of blocked users")
    public ResponseEntity<Long> getBlockedCount() {
        return ResponseEntity.ok(userService.countBlocked());
    }

    @GetMapping("/admin/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Get detailed user statistics for Admin dashboard")
    public ResponseEntity<UserSummaryResponse> getAdminSummary() {
        return ResponseEntity.ok(userService.getUserSummary());
    }
}
