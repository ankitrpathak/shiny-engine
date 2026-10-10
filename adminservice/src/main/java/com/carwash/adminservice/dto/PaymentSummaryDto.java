package com.carwash.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummaryDto {
    private BigDecimal totalRevenue;
    private long totalTransactions;
    private long successfulTransactions;
    private long pendingTransactions;
    private long failedTransactions;
}
