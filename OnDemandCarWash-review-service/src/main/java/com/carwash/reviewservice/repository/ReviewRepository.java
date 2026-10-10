package com.carwash.reviewservice.repository;

import com.carwash.reviewservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByBookingId(Long bookingId);
    boolean existsByBookingId(Long bookingId);
    List<Review> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Review> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);
    List<Review> findByWasherIdOrderByCreatedAtDesc(Long washerId);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r")
    Double calculateAverageRating();

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.washerId = :washerId")
    Double calculateAverageRatingForWasher(Long washerId);
}
