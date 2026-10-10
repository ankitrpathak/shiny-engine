package com.carwash.adminservice.client;

import com.carwash.adminservice.dto.ReviewSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

@FeignClient(name = "review-service")
public interface ReviewClient {

    @GetMapping("/api/reviews")
    List<Map<String, Object>> getAllReviews();

    @GetMapping("/api/reviews/admin/summary")
    ReviewSummaryDto getReviewSummary(@RequestHeader("Authorization") String token);
}
