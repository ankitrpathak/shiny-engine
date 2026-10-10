package com.carwash.reviewservice.service;

import com.carwash.reviewservice.client.BookingClient;
import com.carwash.reviewservice.dto.BookingDto;
import com.carwash.reviewservice.dto.CreateReviewRequest;
import com.carwash.reviewservice.dto.ReviewResponse;
import com.carwash.reviewservice.dto.ReviewSummaryResponse;
import com.carwash.reviewservice.entity.Review;
import com.carwash.reviewservice.exception.BadRequestException;
import com.carwash.reviewservice.exception.ResourceNotFoundException;
import com.carwash.reviewservice.exception.UnauthorizedException;
import com.carwash.reviewservice.repository.ReviewRepository;
import com.carwash.reviewservice.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingClient bookingClient;

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, CustomUserDetails currentUser) {
        log.info("Creating review for booking ID: {} by user: {}", request.getBookingId(), currentUser.getEmail());

        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5 stars.");
        }

        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new BadRequestException("A review has already been submitted for this booking.");
        }

        // Verify booking details & ownership
        Long washerId = request.getWasherId();
        String washerName = request.getWasherName();

        try {
            BookingDto booking = bookingClient.getBookingById(request.getBookingId());
            if (booking != null) {
                if (!currentUser.getEmail().equalsIgnoreCase(booking.getCustomerEmail())) {
                    throw new UnauthorizedException("You can only review your own bookings.");
                }
                if (washerId == null) {
                    washerId = booking.getWasherId();
                    washerName = booking.getWasherName();
                }
            }
        } catch (UnauthorizedException ue) {
            throw ue;
        } catch (Exception ex) {
            log.warn("Booking verification via Feign call failed or bypassed (fallback): {}", ex.getMessage());
        }

        Review review = Review.builder()
                .bookingId(request.getBookingId())
                .customerId(currentUser.getUserId())
                .customerEmail(currentUser.getEmail())
                .customerName(currentUser.getUsername())
                .washerId(washerId)
                .washerName(washerName != null ? washerName : "Service Specialist")
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review created successfully with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    public ReviewResponse getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
        return mapToResponse(review);
    }

    @Override
    public ReviewResponse getReviewByBookingId(Long bookingId) {
        Review review = reviewRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for booking ID: " + bookingId));
        return mapToResponse(review);
    }

    @Override
    public List<ReviewResponse> getMyReviews(CustomUserDetails currentUser) {
        return reviewRepository.findByCustomerEmailOrderByCreatedAtDesc(currentUser.getEmail())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getReviewsByWasherId(Long washerId) {
        return reviewRepository.findByWasherIdOrderByCreatedAtDesc(washerId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public ReviewSummaryResponse getReviewSummary() {
        List<Review> all = reviewRepository.findAll();
        long total = all.size();
        double avg = total > 0 ? all.stream().mapToInt(Review::getRating).average().orElse(0.0) : 0.0;

        long five = all.stream().filter(r -> r.getRating() == 5).count();
        long four = all.stream().filter(r -> r.getRating() == 4).count();
        long three = all.stream().filter(r -> r.getRating() == 3).count();
        long two = all.stream().filter(r -> r.getRating() == 2).count();
        long one = all.stream().filter(r -> r.getRating() == 1).count();

        return ReviewSummaryResponse.builder()
                .totalReviews(total)
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .fiveStarCount(five)
                .fourStarCount(four)
                .threeStarCount(three)
                .twoStarCount(two)
                .oneStarCount(one)
                .build();
    }

    private ReviewResponse mapToResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .bookingId(r.getBookingId())
                .customerId(r.getCustomerId())
                .customerEmail(r.getCustomerEmail())
                .customerName(r.getCustomerName())
                .washerId(r.getWasherId())
                .washerName(r.getWasherName())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
