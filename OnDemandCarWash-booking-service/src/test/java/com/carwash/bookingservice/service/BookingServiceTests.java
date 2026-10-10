package com.carwash.bookingservice.service;

import com.carwash.bookingservice.dto.BookingResponse;
import com.carwash.bookingservice.dto.BookingSummaryResponse;
import com.carwash.bookingservice.dto.CreateBookingRequest;
import com.carwash.bookingservice.entity.Booking;
import com.carwash.bookingservice.entity.BookingStatus;
import com.carwash.bookingservice.entity.ServicePackage;
import com.carwash.bookingservice.exception.BadRequestException;
import com.carwash.bookingservice.repository.BookingRepository;
import com.carwash.bookingservice.repository.PackageRepository;
import com.carwash.bookingservice.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTests {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private BookingEventPublisher eventPublisher;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private ServicePackage samplePackage;
    private Booking sampleBooking;
    private CustomUserDetails customerDetails;
    private CustomUserDetails washerDetails;

    @BeforeEach
    void setUp() {
        samplePackage = ServicePackage.builder()
                .id(1L)
                .title("Express Wash")
                .price(new BigDecimal("399.00"))
                .active(true)
                .build();

        sampleBooking = Booking.builder()
                .id(10L)
                .customerId(5L)
                .customerEmail("customer@example.com")
                .servicePackage(samplePackage)
                .amount(new BigDecimal("399.00"))
                .vehicleType("Sedan")
                .vehicleNumber("DL 01 AB 1234")
                .serviceDate(LocalDate.now().plusDays(1))
                .serviceTimeSlot("10:00 AM - 11:00 AM")
                .serviceAddress("Connaught Place, Delhi")
                .status(BookingStatus.PENDING)
                .paid(false)
                .createdAt(LocalDateTime.now())
                .build();

        customerDetails = new CustomUserDetails(5L, "customer@example.com", "CUSTOMER");
        washerDetails = new CustomUserDetails(20L, "washer@example.com", "WASHER");
    }

    @Test
    void createBooking_DerivesAuthoritativeAmountFromPackage() {
        CreateBookingRequest req = CreateBookingRequest.builder()
                .packageId(1L)
                .vehicleType("Sedan")
                .vehicleNumber("DL 01 AB 1234")
                .serviceDate(LocalDate.now().plusDays(1))
                .serviceTimeSlot("10:00 AM - 11:00 AM")
                .serviceAddress("Connaught Place, Delhi")
                .build();

        when(packageRepository.findById(1L)).thenReturn(Optional.of(samplePackage));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> {
            Booking b = i.getArgument(0);
            b.setId(10L);
            b.setCreatedAt(LocalDateTime.now());
            return b;
        });

        BookingResponse response = bookingService.createBooking(req, customerDetails);

        assertNotNull(response);
        assertEquals(new BigDecimal("399.00"), response.getAmount());
        assertEquals(BookingStatus.PENDING, response.getStatus());
        assertEquals("DL 01 AB 1234", response.getVehicleNumber());
        verify(eventPublisher).publishBookingCreated(any());
    }

    @Test
    void createBooking_InactivePackage_ThrowsBadRequest() {
        samplePackage.setActive(false);
        CreateBookingRequest req = CreateBookingRequest.builder()
                .packageId(1L)
                .vehicleType("Sedan")
                .vehicleNumber("DL 01 AB 1234")
                .serviceDate(LocalDate.now().plusDays(1))
                .serviceTimeSlot("10:00 AM - 11:00 AM")
                .serviceAddress("Connaught Place, Delhi")
                .build();

        when(packageRepository.findById(1L)).thenReturn(Optional.of(samplePackage));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(req, customerDetails));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void assignWasher_UpdatesStatusToAssignedAndPublishesEvent() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        BookingResponse response = bookingService.assignWasher(10L, 20L, "Washer Bob");

        assertNotNull(response);
        assertEquals(20L, response.getWasherId());
        assertEquals("Washer Bob", response.getWasherName());
        assertEquals(BookingStatus.ASSIGNED, response.getStatus());
        verify(eventPublisher).publishBookingAssigned(any());
    }

    @Test
    void updateBookingStatus_WasherValidTransition_Success() {
        sampleBooking.setStatus(BookingStatus.ASSIGNED);
        sampleBooking.setWasherId(20L);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        BookingResponse inProgress = bookingService.updateBookingStatus(10L, BookingStatus.IN_PROGRESS, washerDetails);
        assertEquals(BookingStatus.IN_PROGRESS, inProgress.getStatus());

        BookingResponse completed = bookingService.updateBookingStatus(10L, BookingStatus.COMPLETED, washerDetails);
        assertEquals(BookingStatus.COMPLETED, completed.getStatus());
    }

    @Test
    void updateBookingStatus_CustomerCannotCompleteBooking() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(sampleBooking));

        assertThrows(BadRequestException.class, () ->
                bookingService.updateBookingStatus(10L, BookingStatus.COMPLETED, customerDetails));
    }

    @Test
    void getBookingSummary_ReturnsAggregatedMetrics() {
        when(bookingRepository.count()).thenReturn(5L);
        when(bookingRepository.countByStatus(BookingStatus.PENDING)).thenReturn(2L);
        when(bookingRepository.countByStatus(BookingStatus.ASSIGNED)).thenReturn(1L);
        when(bookingRepository.countByStatus(BookingStatus.IN_PROGRESS)).thenReturn(1L);
        when(bookingRepository.countByStatus(BookingStatus.COMPLETED)).thenReturn(1L);
        when(bookingRepository.countByStatus(BookingStatus.CANCELLED)).thenReturn(0L);
        when(bookingRepository.sumTotalPaidRevenue()).thenReturn(new BigDecimal("1500.00"));

        BookingSummaryResponse summary = bookingService.getBookingSummary();

        assertNotNull(summary);
        assertEquals(5L, summary.getTotalBookings());
        assertEquals(2L, summary.getPendingBookings());
        assertEquals(new BigDecimal("1500.00"), summary.getTotalRevenue());
    }
}
