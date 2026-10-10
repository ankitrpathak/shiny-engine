package com.carwash.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {
    private long totalUsers;
    private long totalCustomers;
    private long totalWashers;
    private long activeUsers;
    private long inactiveUsers;
}
