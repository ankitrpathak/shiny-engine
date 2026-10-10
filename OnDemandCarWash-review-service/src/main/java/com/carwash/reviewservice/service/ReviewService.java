package com.carwash.reviewservice.service;

import com.carwash.reviewservice.dto.CreateReviewRequest;
import com.carwash.reviewservice.dto.ReviewResponse;
import com.carwash.reviewservice.dto.ReviewSummaryResponse;
import com.carwash.reviewservice.security.CustomUserDetails;

import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(CreateReviewRequest request, CustomUserDetails currentUser);
    ReviewResponse getReviewById(Long reviewId);
    ReviewResponse getReviewByBookingId(Long bookingId);
    List<ReviewResponse> getMyReviews(CustomUserDetails currentUser);
    List<ReviewResponse> getReviewsByWasherId(Long washerId);
    List<ReviewResponse> getAllReviews();
    ReviewSummaryResponse getReviewSummary();
}
