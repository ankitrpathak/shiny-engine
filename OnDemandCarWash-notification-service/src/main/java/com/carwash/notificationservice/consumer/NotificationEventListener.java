package com.carwash.notificationservice.consumer;

import com.carwash.notificationservice.dto.NotificationRequest;
import com.carwash.notificationservice.entity.NotificationType;
import com.carwash.notificationservice.event.BookingAssignedEvent;
import com.carwash.notificationservice.event.BookingCreatedEvent;
import com.carwash.notificationservice.event.BookingStatusChangedEvent;
import com.carwash.notificationservice.event.PaymentCompletedEvent;
import com.carwash.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${rabbitmq.queue.booking-created:booking.created.queue}")
    public void handleBookingCreated(BookingCreatedEvent event) {
        log.info("Received BookingCreatedEvent for booking ID: {}", event.getBookingId());
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getCustomerId())
                    .userEmail(event.getCustomerEmail())
                    .title("Booking Placed Successfully")
                    .message(String.format("Your booking #%d for %s has been placed. Scheduled for %s (%s).",
                            event.getBookingId(), event.getPackageTitle(), event.getServiceDate(), event.getServiceTimeSlot()))
                    .type(NotificationType.BOOKING_CREATED)
                    .referenceId(event.getBookingId())
                    .build();
            notificationService.createNotification(request);
        } catch (Exception e) {
            log.error("Error processing BookingCreatedEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.booking-assigned:booking.assigned.queue}")
    public void handleBookingAssigned(BookingAssignedEvent event) {
        log.info("Received BookingAssignedEvent for booking ID: {}", event.getBookingId());
        try {
            // Notification for Washer
            NotificationRequest washerNotif = NotificationRequest.builder()
                    .userId(event.getWasherId())
                    .title("New Booking Assigned")
                    .message(String.format("You have been assigned to booking #%d for vehicle %s on %s (%s).",
                            event.getBookingId(), event.getVehicleNumber(), event.getServiceDate(), event.getServiceTimeSlot()))
                    .type(NotificationType.BOOKING_ASSIGNED)
                    .referenceId(event.getBookingId())
                    .build();
            notificationService.createNotification(washerNotif);

            // Notification for Customer
            NotificationRequest customerNotif = NotificationRequest.builder()
                    .userId(event.getCustomerId())
                    .title("Washer Assigned")
                    .message(String.format("Washer %s has been assigned to your booking #%d.",
                            event.getWasherName(), event.getBookingId()))
                    .type(NotificationType.BOOKING_ASSIGNED)
                    .referenceId(event.getBookingId())
                    .build();
            notificationService.createNotification(customerNotif);
        } catch (Exception e) {
            log.error("Error processing BookingAssignedEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.booking-status:booking.status.queue}")
    public void handleBookingStatusChanged(BookingStatusChangedEvent event) {
        log.info("Received BookingStatusChangedEvent for booking ID: {} -> {}", event.getBookingId(), event.getNewStatus());
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getCustomerId())
                    .title("Booking Status Updated")
                    .message(String.format("Your booking #%d status changed to %s.",
                            event.getBookingId(), event.getNewStatus()))
                    .type(NotificationType.STATUS_UPDATE)
                    .referenceId(event.getBookingId())
                    .build();
            notificationService.createNotification(request);
        } catch (Exception e) {
            log.error("Error processing BookingStatusChangedEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.payment-completed:payment.completed.queue}")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received PaymentCompletedEvent for booking ID: {}", event.getBookingId());
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getCustomerId())
                    .userEmail(event.getCustomerEmail())
                    .title("Payment Received")
                    .message(String.format("Payment of ₹%.2f for booking #%d was successful (Payment ID: %s).",
                            event.getAmount(), event.getBookingId(), event.getRazorpayPaymentId()))
                    .type(NotificationType.PAYMENT_SUCCESS)
                    .referenceId(event.getBookingId())
                    .build();
            notificationService.createNotification(request);
        } catch (Exception e) {
            log.error("Error processing PaymentCompletedEvent: {}", e.getMessage(), e);
        }
    }
}
