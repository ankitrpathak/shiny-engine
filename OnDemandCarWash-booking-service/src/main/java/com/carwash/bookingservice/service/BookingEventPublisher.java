package com.carwash.bookingservice.service;

import com.carwash.bookingservice.config.RabbitMQConfig;
import com.carwash.bookingservice.event.BookingAssignedEvent;
import com.carwash.bookingservice.event.BookingCreatedEvent;
import com.carwash.bookingservice.event.BookingStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishBookingCreated(BookingCreatedEvent event) {
        try {
            log.info("Publishing BookingCreatedEvent for booking id: {}", event.getBookingId());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.BOOKING_CREATED_ROUTING_KEY, event);
        } catch (Exception ex) {
            log.warn("Could not publish BookingCreatedEvent to RabbitMQ (service continues): {}", ex.getMessage());
        }
    }

    public void publishBookingAssigned(BookingAssignedEvent event) {
        try {
            log.info("Publishing BookingAssignedEvent for booking id: {}", event.getBookingId());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.BOOKING_ASSIGNED_ROUTING_KEY, event);
        } catch (Exception ex) {
            log.warn("Could not publish BookingAssignedEvent to RabbitMQ (service continues): {}", ex.getMessage());
        }
    }

    public void publishBookingStatusChanged(BookingStatusChangedEvent event) {
        try {
            log.info("Publishing BookingStatusChangedEvent for booking id: {}", event.getBookingId());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.BOOKING_STATUS_ROUTING_KEY, event);
        } catch (Exception ex) {
            log.warn("Could not publish BookingStatusChangedEvent to RabbitMQ (service continues): {}", ex.getMessage());
        }
    }
}
