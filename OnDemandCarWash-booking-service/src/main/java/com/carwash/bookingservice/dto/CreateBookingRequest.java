package com.carwash.bookingservice.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingRequest {

    @NotNull(message = "Service package ID is required")
    private Long packageId;

    @NotBlank(message = "Vehicle type is required (e.g., Sedan, SUV, Hatchback)")
    private String vehicleType;

    @NotBlank(message = "Vehicle registration number is required")
    private String vehicleNumber;

    private String vehicleModel;

    @NotNull(message = "Service date is required")
    @FutureOrPresent(message = "Service date cannot be in the past")
    private LocalDate serviceDate;

    @NotBlank(message = "Service time slot is required")
    private String serviceTimeSlot;

    @NotBlank(message = "Service address is required")
    private String serviceAddress;

    private String customerNotes;
}
