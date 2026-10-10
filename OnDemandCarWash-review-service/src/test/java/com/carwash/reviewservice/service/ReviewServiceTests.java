package com.carwash.reviewservice.service;

import com.carwash.reviewservice.client.BookingClient;
import com.carwash.reviewservice.dto.BookingDto;
import com.carwash.reviewservice.dto.CreateReviewRequest;
import com.carwash.reviewservice.dto.ReviewResponse;
import com.carwash.reviewservice.dto.ReviewSummaryResponse;
import com.carwash.reviewservice.entity.Review;
import com.carwash.reviewservice.exception.BadRequestException;
import com.carwash.reviewservice.repository.ReviewRepository;
import com.carwash.reviewservice.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTests {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookingClient bookingClient;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private CustomUserDetails customerDetails;
    private BookingDto sampleBooking;

    @BeforeEach
    void setUp() {
        customerDetails = new CustomUserDetails(4L, "customer@example.com", "CUSTOMER");
        sampleBooking = BookingDto.builder()
                .id(101L)
                .customerId(4L)
                .customerEmail("customer@example.com")
                .washerId(3L)
                .washerName("Rahul Sharma")
                .status("COMPLETED")
                .paid(true)
                .build();
    }

    @Test
    void createReview_Success() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .bookingId(101L)
                .rating(5)
                .comment("Outstanding wash service!")
                .build();

        when(reviewRepository.existsByBookingId(101L)).thenReturn(false);
        when(bookingClient.getBookingById(101L)).thenReturn(sampleBooking);
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> {
            Review r = i.getArgument(0);
            r.setId(1L);
            r.setCreatedAt(LocalDateTime.now());
            return r;
        });

        ReviewResponse response = reviewService.createReview(request, customerDetails);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("Outstanding wash service!", response.getComment());
        assertEquals("Rahul Sharma", response.getWasherName());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_DuplicateBooking_ThrowsBadRequest() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .bookingId(101L)
                .rating(5)
                .comment("Outstanding wash service!")
                .build();

        when(reviewRepository.existsByBookingId(101L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> reviewService.createReview(request, customerDetails));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_InvalidRating_ThrowsBadRequest() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .bookingId(101L)
                .rating(6) // Invalid rating
                .comment("Bad rating")
                .build();

        assertThrows(BadRequestException.class, () -> reviewService.createReview(request, customerDetails));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void getReviewSummary_CalculatesAverageAndDistribution() {
        List<Review> reviews = List.of(
                Review.builder().id(1L).rating(5).build(),
                Review.builder().id(2L).rating(5).build(),
                Review.builder().id(3L).rating(4).build()
        );

        when(reviewRepository.findAll()).thenReturn(reviews);

        ReviewSummaryResponse summary = reviewService.getReviewSummary();

        assertNotNull(summary);
        assertEquals(3L, summary.getTotalReviews());
        assertEquals(4.7, summary.getAverageRating(), 0.1);
        assertEquals(2L, summary.getFiveStarCount());
        assertEquals(1L, summary.getFourStarCount());
    }
}
