package com.carwash.bookingservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String customerEmail;

    private String customerName;

    private String customerPhone;

    private Long washerId;

    private String washerName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "package_id", nullable = false)
    private ServicePackage servicePackage;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String vehicleType; // Sedans, SUVs, Hatchbacks, Luxury

    @Column(nullable = false)
    private String vehicleNumber;

    private String vehicleModel;

    @Column(nullable = false)
    private LocalDate serviceDate;

    @Column(nullable = false)
    private String serviceTimeSlot; // e.g. "10:00 AM - 11:30 AM"

    @Column(nullable = false)
    private String serviceAddress;

    private String customerNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private boolean paid = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
