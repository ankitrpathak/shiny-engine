package com.carwash.notificationservice.event;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingStatusChangedEvent implements Serializable {
    private Long bookingId;
    private Long customerId;
    private Long washerId;
    private String previousStatus;
    private String newStatus;
    private String reason;
}
