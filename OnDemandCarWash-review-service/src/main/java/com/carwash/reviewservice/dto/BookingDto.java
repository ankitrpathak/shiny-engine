package com.carwash.reviewservice.dto;

import lombok.*;

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
    private Long washerId;
    private String washerName;
    private String status;
    private boolean paid;
}
