package com.carwash.notificationservice.controller;

import com.carwash.notificationservice.dto.NotificationRequest;
import com.carwash.notificationservice.dto.NotificationResponse;
import com.carwash.notificationservice.security.CustomUserDetails;
import com.carwash.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Management", description = "Endpoints for viewing and managing user notifications")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get current user notifications")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails.getUserId() != null) {
            return ResponseEntity.ok(notificationService.getUserNotifications(userDetails.getUserId()));
        }
        return ResponseEntity.ok(notificationService.getUserNotificationsByEmail(userDetails.getUsername()));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userDetails.getUserId()));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread notifications count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        long count = notificationService.getUnreadCount(userDetails.getUserId());
        Map<String, Object> resp = new HashMap<>();
        resp.put("userId", userDetails.getUserId());
        resp.put("unreadCount", count);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark single notification as read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read for current user")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUserId());
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "All notifications marked as read");
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Create notification manually (Admin only)")
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request) {
        return new ResponseEntity<>(notificationService.createNotification(request), HttpStatus.CREATED);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Get all system notifications (Admin only)")
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }
}
