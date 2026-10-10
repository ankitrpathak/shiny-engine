package com.carwash.paymentservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummaryResponse {
    private long totalTransactions;
    private long successfulTransactions;
    private long pendingTransactions;
    private long failedTransactions;
    private BigDecimal totalRevenue;
}
