package com.carwash.bookingservice.config;

import com.carwash.bookingservice.entity.ServicePackage;
import com.carwash.bookingservice.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PackageRepository packageRepository;

    @Override
    public void run(String... args) {
        if (packageRepository.count() == 0) {
            log.info("Seeding standard service packages catalog...");
            packageRepository.save(ServicePackage.builder()
                    .title("Express Exterior Wash")
                    .description("Fast & thorough exterior wash using pH-balanced snow foam shampoo, high pressure rinse, tire gloss, and hand microfiber dry.")
                    .price(new BigDecimal("399.00"))
                    .estimatedDurationMinutes(30)
                    .features("High-pressure exterior wash, Tire dressing & gloss, Rim brake-dust cleanup, Streak-free window polish, Microfiber dry")
                    .active(true)
                    .build());

            packageRepository.save(ServicePackage.builder()
                    .title("Interior Detail & Sanitization")
                    .description("Complete cabin restoration with antimicrobial steam cleaning, deep vacuuming, upholstery care, and air conditioning vent disinfection.")
                    .price(new BigDecimal("799.00"))
                    .estimatedDurationMinutes(45)
                    .features("Deep interior cabin vacuuming, AC vent antibacterial steam, Dashboard & console UV protection, Mat cleaning, Odor eliminator")
                    .active(true)
                    .build());

            packageRepository.save(ServicePackage.builder()
                    .title("Full Premium Spa")
                    .description("The ultimate bumper-to-bumper detailing package combining exterior gloss enhancement with deep interior sanitization.")
                    .price(new BigDecimal("1499.00"))
                    .estimatedDurationMinutes(75)
                    .features("Everything in Exterior + Interior, Carnauba protective hand wax, Leather conditioning treatment, Engine bay rinse & dressing, Rain-repellent windshield shield")
                    .active(true)
                    .build());

            packageRepository.save(ServicePackage.builder()
                    .title("Ceramic Pro Shield & Polish")
                    .description("Showroom finish with single-stage machine buffing to remove swirls and 9H nano-ceramic hydrophobic shield application.")
                    .price(new BigDecimal("2999.00"))
                    .estimatedDurationMinutes(120)
                    .features("Single-stage paint polish & swirl removal, 9H Nano-ceramic hydrophobic sealant, 6-month UV protection, Ceramic rim shield, Glass rain repeller")
                    .active(true)
                    .build());

            log.info("4 default service packages seeded successfully.");
        }
    }
}
