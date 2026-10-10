package com.carwash.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminActionResponse {
    private Long id;
    private Long adminId;
    private String adminEmail;
    private String actionType;
    private String targetResource;
    private Long targetId;
    private String details;
    private LocalDateTime performedAt;
}
