package com.carwash.bookingservice.event;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreatedEvent implements Serializable {
    private Long bookingId;
    private Long customerId;
    private String customerEmail;
    private String customerName;
    private String packageTitle;
    private BigDecimal amount;
    private LocalDate serviceDate;
    private String serviceTimeSlot;
}
