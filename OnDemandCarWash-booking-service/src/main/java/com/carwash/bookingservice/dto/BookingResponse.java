package com.carwash.bookingservice.dto;

import com.carwash.bookingservice.entity.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
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
    private String vehicleType;
    private String vehicleNumber;
    private String vehicleModel;
    private LocalDate serviceDate;
    private String serviceTimeSlot;
    private String serviceAddress;
    private String customerNotes;
    private BookingStatus status;
    private boolean paid;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
