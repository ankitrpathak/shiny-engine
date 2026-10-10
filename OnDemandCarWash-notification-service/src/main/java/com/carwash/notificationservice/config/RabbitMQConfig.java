package com.carwash.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "carwash.exchange";

    public static final String BOOKING_CREATED_QUEUE = "booking.created.queue";
    public static final String BOOKING_ASSIGNED_QUEUE = "booking.assigned.queue";
    public static final String BOOKING_STATUS_QUEUE = "booking.status.queue";
    public static final String PAYMENT_COMPLETED_QUEUE = "payment.completed.queue";

    public static final String BOOKING_CREATED_ROUTING_KEY = "booking.created";
    public static final String BOOKING_ASSIGNED_ROUTING_KEY = "booking.assigned";
    public static final String BOOKING_STATUS_ROUTING_KEY = "booking.status.changed";
    public static final String PAYMENT_COMPLETED_ROUTING_KEY = "payment.completed";

    @Bean
    public TopicExchange carwashExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue bookingCreatedQueue() {
        return QueueBuilder.durable(BOOKING_CREATED_QUEUE).build();
    }

    @Bean
    public Queue bookingAssignedQueue() {
        return QueueBuilder.durable(BOOKING_ASSIGNED_QUEUE).build();
    }

    @Bean
    public Queue bookingStatusQueue() {
        return QueueBuilder.durable(BOOKING_STATUS_QUEUE).build();
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return QueueBuilder.durable(PAYMENT_COMPLETED_QUEUE).build();
    }

    @Bean
    public Binding bookingCreatedBinding(Queue bookingCreatedQueue, TopicExchange carwashExchange) {
        return BindingBuilder.bind(bookingCreatedQueue).to(carwashExchange).with(BOOKING_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding bookingAssignedBinding(Queue bookingAssignedQueue, TopicExchange carwashExchange) {
        return BindingBuilder.bind(bookingAssignedQueue).to(carwashExchange).with(BOOKING_ASSIGNED_ROUTING_KEY);
    }

    @Bean
    public Binding bookingStatusBinding(Queue bookingStatusQueue, TopicExchange carwashExchange) {
        return BindingBuilder.bind(bookingStatusQueue).to(carwashExchange).with(BOOKING_STATUS_ROUTING_KEY);
    }

    @Bean
    public Binding paymentCompletedBinding(Queue paymentCompletedQueue, TopicExchange carwashExchange) {
        return BindingBuilder.bind(paymentCompletedQueue).to(carwashExchange).with(PAYMENT_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
