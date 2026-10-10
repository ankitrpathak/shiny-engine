package com.carwash.paymentservice.service;

import com.carwash.paymentservice.config.RabbitMQConfig;
import com.carwash.paymentservice.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        try {
            log.info("Publishing PaymentCompletedEvent for payment id: {}", event.getPaymentId());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.PAYMENT_COMPLETED_ROUTING_KEY, event);
        } catch (Exception ex) {
            log.warn("Could not publish PaymentCompletedEvent to RabbitMQ (service continues): {}", ex.getMessage());
        }
    }
}
