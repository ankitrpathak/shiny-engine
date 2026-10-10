package com.carwash.bookingservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer estimatedDurationMinutes;
    private String features;
    private boolean active;
    private LocalDateTime createdAt;
}
