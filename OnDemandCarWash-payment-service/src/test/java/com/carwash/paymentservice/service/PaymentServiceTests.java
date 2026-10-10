package com.carwash.paymentservice.service;

import com.carwash.paymentservice.client.BookingClient;
import com.carwash.paymentservice.dto.BookingDto;
import com.carwash.paymentservice.dto.CreateOrderRequest;
import com.carwash.paymentservice.dto.RazorpayOrderResponse;
import com.carwash.paymentservice.dto.VerifyPaymentRequest;
import com.carwash.paymentservice.entity.Payment;
import com.carwash.paymentservice.entity.PaymentStatus;
import com.carwash.paymentservice.exception.BadRequestException;
import com.carwash.paymentservice.repository.PaymentRepository;
import com.carwash.paymentservice.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTests {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingClient bookingClient;

    @Mock
    private PaymentEventPublisher eventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private CustomUserDetails customerDetails;
    private BookingDto sampleBooking;
    private Payment samplePayment;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "razorpayKeyId", "rzp_test_carwash2026");
        ReflectionTestUtils.setField(paymentService, "razorpayKeySecret", "carwashrazorpaysecret2026");

        customerDetails = new CustomUserDetails(10L, "customer@example.com", "CUSTOMER");

        sampleBooking = BookingDto.builder()
                .id(100L)
                .customerId(10L)
                .customerEmail("customer@example.com")
                .customerName("John Doe")
                .amount(new BigDecimal("799.00"))
                .packageTitle("Interior Detail")
                .paid(false)
                .build();

        samplePayment = Payment.builder()
                .id(1L)
                .bookingId(100L)
                .customerId(10L)
                .customerEmail("customer@example.com")
                .amount(new BigDecimal("799.00"))
                .currency("INR")
                .razorpayOrderId("order_test_100")
                .status(PaymentStatus.PENDING)
                .build();
    }

    @Test
    void createRazorpayOrder_DerivesAmountAndConvertsToPaise() {
        CreateOrderRequest request = CreateOrderRequest.builder().bookingId(100L).build();

        when(bookingClient.getBookingById(100L)).thenReturn(sampleBooking);
        when(paymentRepository.findByBookingId(100L)).thenReturn(Optional.of(samplePayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        RazorpayOrderResponse response = paymentService.createRazorpayOrder(request, customerDetails);

        assertNotNull(response);
        assertEquals(new BigDecimal("799.00"), response.getAmount());
        assertEquals(79900L, response.getAmountInPaise());
        assertEquals("INR", response.getCurrency());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createRazorpayOrder_AlreadyPaid_ThrowsBadRequest() {
        sampleBooking.setPaid(true);
        CreateOrderRequest request = CreateOrderRequest.builder().bookingId(100L).build();

        when(bookingClient.getBookingById(100L)).thenReturn(sampleBooking);

        assertThrows(BadRequestException.class, () -> paymentService.createRazorpayOrder(request, customerDetails));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void verifyPayment_ValidTestSignature_UpdatesToSuccess() {
        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .bookingId(100L)
                .razorpayOrderId("order_test_100")
                .razorpayPaymentId("pay_test_999")
                .razorpaySignature("test_sig_sample")
                .paymentMethod("UPI")
                .build();

        when(paymentRepository.findByBookingId(100L)).thenReturn(Optional.of(samplePayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        var response = paymentService.verifyPayment(request, customerDetails);

        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals("pay_test_999", response.getRazorpayPaymentId());
        verify(bookingClient).markBookingPaid(100L);
        verify(eventPublisher).publishPaymentCompleted(any());
    }

    @Test
    void verifyPayment_InvalidSignature_ThrowsBadRequest() {
        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .bookingId(100L)
                .razorpayOrderId("order_test_100")
                .razorpayPaymentId("pay_test_999")
                .razorpaySignature("invalid_forged_signature")
                .build();

        when(paymentRepository.findByBookingId(100L)).thenReturn(Optional.of(samplePayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        assertThrows(BadRequestException.class, () -> paymentService.verifyPayment(request, customerDetails));
        assertEquals(PaymentStatus.FAILED, samplePayment.getStatus());
        verify(bookingClient, never()).markBookingPaid(anyLong());
    }
}
