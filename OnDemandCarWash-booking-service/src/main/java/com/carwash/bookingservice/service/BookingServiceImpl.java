package com.carwash.bookingservice.service;

import com.carwash.bookingservice.dto.BookingResponse;
import com.carwash.bookingservice.dto.BookingSummaryResponse;
import com.carwash.bookingservice.dto.CreateBookingRequest;
import com.carwash.bookingservice.entity.Booking;
import com.carwash.bookingservice.entity.BookingStatus;
import com.carwash.bookingservice.entity.ServicePackage;
import com.carwash.bookingservice.event.BookingAssignedEvent;
import com.carwash.bookingservice.event.BookingCreatedEvent;
import com.carwash.bookingservice.event.BookingStatusChangedEvent;
import com.carwash.bookingservice.exception.BadRequestException;
import com.carwash.bookingservice.exception.ResourceNotFoundException;
import com.carwash.bookingservice.exception.UnauthorizedException;
import com.carwash.bookingservice.repository.BookingRepository;
import com.carwash.bookingservice.repository.PackageRepository;
import com.carwash.bookingservice.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PackageRepository packageRepository;
    private final BookingEventPublisher eventPublisher;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, CustomUserDetails currentUser) {
        log.info("Creating booking for customer: {}", currentUser.getEmail());

        ServicePackage servicePackage = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new ResourceNotFoundException("Service package not found with id: " + request.getPackageId()));

        if (!servicePackage.isActive()) {
            throw new BadRequestException("The selected package is currently inactive and cannot be booked.");
        }

        Booking booking = Booking.builder()
                .customerId(currentUser.getUserId())
                .customerEmail(currentUser.getEmail())
                .servicePackage(servicePackage)
                .amount(servicePackage.getPrice()) // Authoritative amount derived directly from package
                .vehicleType(request.getVehicleType())
                .vehicleNumber(request.getVehicleNumber().toUpperCase().trim())
                .vehicleModel(request.getVehicleModel())
                .serviceDate(request.getServiceDate())
                .serviceTimeSlot(request.getServiceTimeSlot())
                .serviceAddress(request.getServiceAddress())
                .customerNotes(request.getCustomerNotes())
                .status(BookingStatus.PENDING)
                .paid(false)
                .build();

        Booking saved = bookingRepository.save(booking);

        // Publish event to RabbitMQ
        eventPublisher.publishBookingCreated(BookingCreatedEvent.builder()
                .bookingId(saved.getId())
                .customerId(saved.getCustomerId())
                .customerEmail(saved.getCustomerEmail())
                .packageTitle(servicePackage.getTitle())
                .amount(saved.getAmount())
                .serviceDate(saved.getServiceDate())
                .serviceTimeSlot(saved.getServiceTimeSlot())
                .build());

        return mapToResponse(saved);
    }

    @Override
    public BookingResponse getBookingById(Long bookingId, CustomUserDetails currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        validateBookingAccess(booking, currentUser);
        return mapToResponse(booking);
    }

    @Override
    public List<BookingResponse> getMyBookings(CustomUserDetails currentUser) {
        return bookingRepository.findByCustomerEmailOrderByCreatedAtDesc(currentUser.getEmail())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getMyAssignments(CustomUserDetails currentUser) {
        if (!"WASHER".equalsIgnoreCase(currentUser.getRole()) &&
            !"ADMIN".equalsIgnoreCase(currentUser.getRole()) &&
            !"GOD".equalsIgnoreCase(currentUser.getRole())) {
            throw new UnauthorizedException("Only washers can access assigned tasks.");
        }
        return bookingRepository.findByWasherIdOrderByCreatedAtDesc(currentUser.getUserId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponse assignWasher(Long bookingId, Long washerId, String washerName) {
        log.info("Assigning washer {} to booking {}", washerId, bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Cannot assign washer to a " + booking.getStatus() + " booking.");
        }

        booking.setWasherId(washerId);
        booking.setWasherName(washerName);
        if (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED) {
            booking.setStatus(BookingStatus.ASSIGNED);
        }

        Booking updated = bookingRepository.save(booking);

        eventPublisher.publishBookingAssigned(BookingAssignedEvent.builder()
                .bookingId(updated.getId())
                .customerId(updated.getCustomerId())
                .washerId(washerId)
                .washerName(washerName)
                .vehicleNumber(updated.getVehicleNumber())
                .serviceDate(updated.getServiceDate())
                .serviceTimeSlot(updated.getServiceTimeSlot())
                .build());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public BookingResponse updateBookingStatus(Long bookingId, BookingStatus newStatus, CustomUserDetails currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        BookingStatus prevStatus = booking.getStatus();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole()) || "GOD".equalsIgnoreCase(currentUser.getRole());
        boolean isWasher = "WASHER".equalsIgnoreCase(currentUser.getRole()) &&
                currentUser.getUserId() != null && currentUser.getUserId().equals(booking.getWasherId());
        boolean isCustomer = currentUser.getEmail().equalsIgnoreCase(booking.getCustomerEmail());

        if (isAdmin) {
            booking.setStatus(newStatus);
        } else if (isWasher) {
            if (newStatus == BookingStatus.IN_PROGRESS && (prevStatus == BookingStatus.ASSIGNED || prevStatus == BookingStatus.CONFIRMED)) {
                booking.setStatus(BookingStatus.IN_PROGRESS);
            } else if (newStatus == BookingStatus.COMPLETED && prevStatus == BookingStatus.IN_PROGRESS) {
                booking.setStatus(BookingStatus.COMPLETED);
            } else {
                throw new BadRequestException("Washer can only transition: ASSIGNED -> IN_PROGRESS -> COMPLETED");
            }
        } else if (isCustomer) {
            if (newStatus == BookingStatus.CANCELLED && (prevStatus == BookingStatus.PENDING || prevStatus == BookingStatus.ASSIGNED)) {
                booking.setStatus(BookingStatus.CANCELLED);
            } else {
                throw new BadRequestException("Customer can only cancel PENDING or ASSIGNED bookings.");
            }
        } else {
            throw new UnauthorizedException("You are not authorized to update this booking's status.");
        }

        Booking updated = bookingRepository.save(booking);

        eventPublisher.publishBookingStatusChanged(BookingStatusChangedEvent.builder()
                .bookingId(updated.getId())
                .customerId(updated.getCustomerId())
                .washerId(updated.getWasherId())
                .previousStatus(prevStatus.name())
                .newStatus(newStatus.name())
                .reason("Status updated by " + currentUser.getRole())
                .build());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public BookingResponse markPaid(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        booking.setPaid(true);
        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
        }
        return mapToResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingSummaryResponse getBookingSummary() {
        return BookingSummaryResponse.builder()
                .totalBookings(bookingRepository.count())
                .pendingBookings(bookingRepository.countByStatus(BookingStatus.PENDING))
                .assignedBookings(bookingRepository.countByStatus(BookingStatus.ASSIGNED))
                .inProgressBookings(bookingRepository.countByStatus(BookingStatus.IN_PROGRESS))
                .completedBookings(bookingRepository.countByStatus(BookingStatus.COMPLETED))
                .cancelledBookings(bookingRepository.countByStatus(BookingStatus.CANCELLED))
                .totalRevenue(bookingRepository.sumTotalPaidRevenue())
                .build();
    }

    private void validateBookingAccess(Booking booking, CustomUserDetails currentUser) {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole()) || "GOD".equalsIgnoreCase(currentUser.getRole());
        boolean isWasher = "WASHER".equalsIgnoreCase(currentUser.getRole()) &&
                currentUser.getUserId() != null && currentUser.getUserId().equals(booking.getWasherId());
        boolean isOwner = currentUser.getEmail().equalsIgnoreCase(booking.getCustomerEmail());

        if (!isAdmin && !isWasher && !isOwner) {
            throw new UnauthorizedException("Access denied: You do not own this booking.");
        }
    }

    private BookingResponse mapToResponse(Booking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .customerId(b.getCustomerId())
                .customerEmail(b.getCustomerEmail())
                .customerName(b.getCustomerName())
                .customerPhone(b.getCustomerPhone())
                .washerId(b.getWasherId())
                .washerName(b.getWasherName())
                .packageId(b.getServicePackage().getId())
                .packageTitle(b.getServicePackage().getTitle())
                .amount(b.getAmount())
                .vehicleType(b.getVehicleType())
                .vehicleNumber(b.getVehicleNumber())
                .vehicleModel(b.getVehicleModel())
                .serviceDate(b.getServiceDate())
                .serviceTimeSlot(b.getServiceTimeSlot())
                .serviceAddress(b.getServiceAddress())
                .customerNotes(b.getCustomerNotes())
                .status(b.getStatus())
                .paid(b.isPaid())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
