package com.carwash.paymentservice.controller;

import com.carwash.paymentservice.dto.*;
import com.carwash.paymentservice.entity.PaymentStatus;
import com.carwash.paymentservice.security.CustomUserDetails;
import com.carwash.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payments and Razorpay Integration Endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    @Operation(summary = "Create Razorpay Order in test mode with INR currency and amount in paise")
    public ResponseEntity<RazorpayOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return new ResponseEntity<>(paymentService.createRazorpayOrder(request, currentUser), HttpStatus.CREATED);
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify Razorpay checkout signature and update payment status to SUCCESS")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(paymentService.verifyPayment(request, currentUser));
    }

    @PostMapping
    @Operation(summary = "Record direct payment entry")
    public ResponseEntity<PaymentResponse> recordPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return new ResponseEntity<>(paymentService.recordPayment(request, currentUser), HttpStatus.CREATED);
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment record by ID")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId, currentUser));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get payment details for a specific booking")
    public ResponseEntity<PaymentResponse> getPaymentByBookingId(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(paymentService.getPaymentByBookingId(bookingId, currentUser));
    }

    @GetMapping("/my-payments")
    @Operation(summary = "Get payment history for current customer")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(paymentService.getMyPayments(currentUser));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Get all platform payments (Admin only)")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @PutMapping("/{paymentId}/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Update payment status (Admin only)")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable Long paymentId,
            @PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(paymentId, status));
    }

    @GetMapping("/admin/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Get payments revenue and transaction metrics for Admin dashboard")
    public ResponseEntity<PaymentSummaryResponse> getPaymentSummary() {
        return ResponseEntity.ok(paymentService.getPaymentSummary());
    }
}
