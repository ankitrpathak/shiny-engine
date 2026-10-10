package com.carwash.reviewservice.controller;

import com.carwash.reviewservice.dto.CreateReviewRequest;
import com.carwash.reviewservice.dto.ReviewResponse;
import com.carwash.reviewservice.dto.ReviewSummaryResponse;
import com.carwash.reviewservice.security.CustomUserDetails;
import com.carwash.reviewservice.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Customer feedback, ratings, and testimonials API")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Submit a review for a completed booking (Customer only)")
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return new ResponseEntity<>(reviewService.createReview(request, currentUser), HttpStatus.CREATED);
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.getReviewById(reviewId));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get review for a specific booking")
    public ResponseEntity<ReviewResponse> getReviewByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(reviewService.getReviewByBookingId(bookingId));
    }

    @GetMapping("/my-reviews")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all reviews submitted by currently logged-in customer")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(reviewService.getMyReviews(currentUser));
    }

    @GetMapping("/washer/{washerId}")
    @Operation(summary = "Get all reviews for a specific washer")
    public ResponseEntity<List<ReviewResponse>> getReviewsByWasherId(@PathVariable Long washerId) {
        return ResponseEntity.ok(reviewService.getReviewsByWasherId(washerId));
    }

    @GetMapping
    @Operation(summary = "List all reviews and ratings across platform")
    public ResponseEntity<List<ReviewResponse>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/admin/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get review statistics and ratings distribution for Admin dashboard")
    public ResponseEntity<ReviewSummaryResponse> getReviewSummary() {
        return ResponseEntity.ok(reviewService.getReviewSummary());
    }
}
