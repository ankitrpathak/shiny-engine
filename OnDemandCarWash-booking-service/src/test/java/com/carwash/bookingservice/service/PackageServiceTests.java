package com.carwash.bookingservice.service;

import com.carwash.bookingservice.dto.PackageRequest;
import com.carwash.bookingservice.dto.PackageResponse;
import com.carwash.bookingservice.entity.ServicePackage;
import com.carwash.bookingservice.repository.PackageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageServiceTests {

    @Mock
    private PackageRepository packageRepository;

    @InjectMocks
    private PackageServiceImpl packageService;

    @Test
    void createPackage_Success() {
        PackageRequest request = PackageRequest.builder()
                .title("Deluxe Wash")
                .description("Great wash")
                .price(new BigDecimal("500.00"))
                .estimatedDurationMinutes(40)
                .build();

        ServicePackage saved = ServicePackage.builder()
                .id(1L)
                .title("Deluxe Wash")
                .description("Great wash")
                .price(new BigDecimal("500.00"))
                .estimatedDurationMinutes(40)
                .active(true)
                .build();

        when(packageRepository.save(any(ServicePackage.class))).thenReturn(saved);

        PackageResponse response = packageService.createPackage(request);

        assertNotNull(response);
        assertEquals("Deluxe Wash", response.getTitle());
        assertTrue(response.isActive());
    }

    @Test
    void disablePackage_Success() {
        ServicePackage existing = ServicePackage.builder()
                .id(1L)
                .title("Deluxe Wash")
                .price(new BigDecimal("500.00"))
                .active(true)
                .build();

        when(packageRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(packageRepository.save(any(ServicePackage.class))).thenAnswer(i -> i.getArgument(0));

        PackageResponse response = packageService.disablePackage(1L);

        assertFalse(response.isActive());
    }
}
