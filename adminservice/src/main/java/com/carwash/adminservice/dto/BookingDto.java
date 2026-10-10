package com.carwash.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;
    private Long customerId;
    private String customerEmail;
    private String customerName;
    private String customerPhone;
    private Long washerId;
    private String washerName;
    private Long packageId;
    private String packageTitle;
    private BigDecimal amount;
    private String vehicleModel;
    private String vehicleNumber;
    private String address;
    private LocalDate serviceDate;
    private String serviceTimeSlot;
    private String status;
    private boolean paid;
    private LocalDateTime createdAt;
}
