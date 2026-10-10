package com.carwash.reviewservice.config;

import com.carwash.reviewservice.entity.Review;
import com.carwash.reviewservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ReviewRepository reviewRepository;

    @Override
    public void run(String... args) {
        if (reviewRepository.count() == 0) {
            log.info("Seeding initial verified customer reviews...");
            reviewRepository.save(Review.builder()
                    .bookingId(101L)
                    .customerId(4L)
                    .customerEmail("customer@carwash.com")
                    .customerName("Priya Patel")
                    .washerId(3L)
                    .washerName("Rahul Sharma (Washer)")
                    .rating(5)
                    .comment("Incredible detailing! The car looks brand new as if it just rolled out of the showroom. Super punctual and courteous specialist.")
                    .build());

            reviewRepository.save(Review.builder()
                    .bookingId(102L)
                    .customerId(5L)
                    .customerEmail("vikram@example.com")
                    .customerName("Vikram Singh")
                    .washerId(3L)
                    .washerName("Rahul Sharma (Washer)")
                    .rating(5)
                    .comment("The steam vent interior cleaning was phenomenal. Completely eliminated stubborn odors and dust from the AC vents. Highly recommended!")
                    .build());

            reviewRepository.save(Review.builder()
                    .bookingId(103L)
                    .customerId(6L)
                    .customerEmail("ananya@example.com")
                    .customerName("Ananya Roy")
                    .washerId(3L)
                    .washerName("Rahul Sharma (Washer)")
                    .rating(4)
                    .comment("Great doorstep service in our society parking lot. The high pressure wash and microfiber dry was thorough with zero water mess.")
                    .build());

            log.info("Initial verified reviews seeded.");
        }
    }
}
