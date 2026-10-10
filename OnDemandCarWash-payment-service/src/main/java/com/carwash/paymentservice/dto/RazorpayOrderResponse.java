package com.carwash.paymentservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayOrderResponse {
    private String orderId;
    private Long bookingId;
    private BigDecimal amount;
    private Long amountInPaise;
    private String currency;
    private String keyId;
    private String customerEmail;
    private String customerName;
    private String customerPhone;
    private String notes;
}
