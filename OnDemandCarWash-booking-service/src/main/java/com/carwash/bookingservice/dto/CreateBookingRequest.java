package com.carwash.bookingservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
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

    @Builder.Default
    private String vehicleType = "Sedan";

    @NotBlank(message = "Vehicle registration number is required")
    private String vehicleNumber;

    private String vehicleModel;

    @NotNull(message = "Service date is required")
    private LocalDate serviceDate;

    @NotBlank(message = "Service time slot is required")
    private String serviceTimeSlot;

    @NotBlank(message = "Service address is required")
    @JsonAlias({"address", "serviceAddress"})
    private String serviceAddress;

    @JsonAlias({"notes", "customerNotes"})
    private String customerNotes;
}
