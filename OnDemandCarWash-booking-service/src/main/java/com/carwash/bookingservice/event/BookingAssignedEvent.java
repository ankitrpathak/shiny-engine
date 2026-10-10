package com.carwash.bookingservice.event;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingAssignedEvent implements Serializable {
    private Long bookingId;
    private Long customerId;
    private Long washerId;
    private String washerName;
    private String vehicleNumber;
    private LocalDate serviceDate;
    private String serviceTimeSlot;
}
