package com.carwash.notificationservice.consumer;

import com.carwash.notificationservice.dto.NotificationRequest;
import com.carwash.notificationservice.event.BookingAssignedEvent;
import com.carwash.notificationservice.event.BookingCreatedEvent;
import com.carwash.notificationservice.event.BookingStatusChangedEvent;
import com.carwash.notificationservice.event.PaymentCompletedEvent;
import com.carwash.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTests {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener eventListener;

    @Test
    void testHandleBookingCreated() {
        BookingCreatedEvent event = BookingCreatedEvent.builder()
                .bookingId(10L)
                .customerId(1L)
                .customerEmail("customer@carwash.com")
                .packageTitle("Full Wash")
                .serviceDate(LocalDate.now().plusDays(1))
                .serviceTimeSlot("10:00 - 11:00")
                .amount(new BigDecimal("499.00"))
                .build();

        eventListener.handleBookingCreated(event);

        verify(notificationService, times(1)).createNotification(any(NotificationRequest.class));
    }

    @Test
    void testHandleBookingAssigned() {
        BookingAssignedEvent event = BookingAssignedEvent.builder()
                .bookingId(10L)
                .customerId(1L)
                .washerId(2L)
                .washerName("Speedy Washer")
                .vehicleNumber("MH01AB1234")
                .serviceDate(LocalDate.now().plusDays(1))
                .serviceTimeSlot("10:00 - 11:00")
                .build();

        eventListener.handleBookingAssigned(event);

        // Dispatches 2 notifications (one for washer, one for customer)
        verify(notificationService, times(2)).createNotification(any(NotificationRequest.class));
    }

    @Test
    void testHandleBookingStatusChanged() {
        BookingStatusChangedEvent event = BookingStatusChangedEvent.builder()
                .bookingId(10L)
                .customerId(1L)
                .previousStatus("ASSIGNED")
                .newStatus("IN_PROGRESS")
                .build();

        eventListener.handleBookingStatusChanged(event);

        verify(notificationService, times(1)).createNotification(any(NotificationRequest.class));
    }

    @Test
    void testHandlePaymentCompleted() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .bookingId(10L)
                .customerId(1L)
                .customerEmail("customer@carwash.com")
                .amount(new BigDecimal("499.00"))
                .razorpayPaymentId("pay_123456")
                .status("SUCCESS")
                .build();

        eventListener.handlePaymentCompleted(event);

        verify(notificationService, times(1)).createNotification(any(NotificationRequest.class));
    }
}
