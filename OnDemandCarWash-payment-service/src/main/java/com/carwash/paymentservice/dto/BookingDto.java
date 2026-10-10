package com.carwash.paymentservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDto {
    private Long id;
    private Long customerId;
    private String customerEmail;
    private String customerName;
    private String customerPhone;
    private BigDecimal amount;
    private String packageTitle;
    private String status;
    private boolean paid;
}
