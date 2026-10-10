package com.carwash.userservice.config;

import com.carwash.userservice.entity.Role;
import com.carwash.userservice.entity.User;
import com.carwash.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("god@carwash.com", "GodAdmin@2026!", "God Super Administrator", "+91 9999900000", "Root Headquarters", Role.GOD);
        seedUser("admin@carwash.com", "Admin@123", "System Administrator", "+91 9888800001", "Ops Center Delhi", Role.ADMIN);
        seedUser("washer@carwash.com", "Washer@123", "Rahul Sharma (Washer)", "+91 9777700002", "Service Hub 1", Role.WASHER);
        seedUser("customer@carwash.com", "Customer@123", "Priya Patel (Customer)", "+91 9666600003", "B-102 Green Park, Delhi", Role.CUSTOMER);
        log.info("Default seed users check completed.");
    }

    private void seedUser(String email, String rawPassword, String fullName, String phone, String address, Role role) {
        if (!userRepository.existsByEmail(email)) {
            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .fullName(fullName)
                    .phone(phone)
                    .address(address)
                    .role(role)
                    .active(true)
                    .build();
            userRepository.save(user);
            log.info("Seeded initial user: {} with role: {}", email, role);
        }
    }
}
