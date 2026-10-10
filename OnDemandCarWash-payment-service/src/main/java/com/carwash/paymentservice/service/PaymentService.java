package com.carwash.paymentservice.service;

import com.carwash.paymentservice.dto.*;
import com.carwash.paymentservice.entity.PaymentStatus;
import com.carwash.paymentservice.security.CustomUserDetails;

import java.util.List;

public interface PaymentService {
    RazorpayOrderResponse createRazorpayOrder(CreateOrderRequest request, CustomUserDetails currentUser);
    PaymentResponse verifyPayment(VerifyPaymentRequest request, CustomUserDetails currentUser);
    PaymentResponse recordPayment(PaymentRequest request, CustomUserDetails currentUser);
    PaymentResponse getPaymentById(Long paymentId, CustomUserDetails currentUser);
    PaymentResponse getPaymentByBookingId(Long bookingId, CustomUserDetails currentUser);
    PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatus status);
    List<PaymentResponse> getMyPayments(CustomUserDetails currentUser);
    List<PaymentResponse> getAllPayments();
    PaymentSummaryResponse getPaymentSummary();
}
