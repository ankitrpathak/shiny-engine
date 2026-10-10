package com.carwash.bookingservice.repository;

import com.carwash.bookingservice.entity.Booking;
import com.carwash.bookingservice.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Booking> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);
    List<Booking> findByWasherIdOrderByCreatedAtDesc(Long washerId);
    List<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status);
    long countByStatus(BookingStatus status);
    
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Booking b WHERE b.paid = true")
    BigDecimal sumTotalPaidRevenue();
}
