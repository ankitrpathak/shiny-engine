package com.carwash.userservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryResponse {
    private long totalUsers;
    private long activeUsers;
    private long blockedUsers;
    private long customers;
    private long washers;
    private long admins;
}
