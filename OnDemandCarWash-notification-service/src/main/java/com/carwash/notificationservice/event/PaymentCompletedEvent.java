package com.carwash.notificationservice.event;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCompletedEvent implements Serializable {
    private Long paymentId;
    private Long bookingId;
    private Long customerId;
    private String customerEmail;
    private BigDecimal amount;
    private String razorpayPaymentId;
    private String status;
}
