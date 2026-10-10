package com.carwash.reviewservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long id;
    private Long bookingId;
    private Long customerId;
    private String customerEmail;
    private String customerName;
    private Long washerId;
    private String washerName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
