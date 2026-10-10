package com.carwash.bookingservice.service;

import com.carwash.bookingservice.dto.PackageRequest;
import com.carwash.bookingservice.dto.PackageResponse;
import com.carwash.bookingservice.entity.ServicePackage;
import com.carwash.bookingservice.exception.ResourceNotFoundException;
import com.carwash.bookingservice.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageServiceImpl implements PackageService {

    private final PackageRepository packageRepository;

    @Override
    @Transactional
    public PackageResponse createPackage(PackageRequest request) {
        log.info("Creating new service package: {}", request.getTitle());
        ServicePackage servicePackage = ServicePackage.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .estimatedDurationMinutes(request.getEstimatedDurationMinutes())
                .features(request.getFeatures())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        ServicePackage saved = packageRepository.save(servicePackage);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public PackageResponse updatePackage(Long packageId, PackageRequest request) {
        log.info("Updating service package id: {}", packageId);
        ServicePackage servicePackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Service package not found with id: " + packageId));

        servicePackage.setTitle(request.getTitle());
        servicePackage.setDescription(request.getDescription());
        servicePackage.setPrice(request.getPrice());
        servicePackage.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        servicePackage.setFeatures(request.getFeatures());
        if (request.getActive() != null) {
            servicePackage.setActive(request.getActive());
        }

        ServicePackage updated = packageRepository.save(servicePackage);
        return mapToResponse(updated);
    }

    @Override
    public PackageResponse getPackageById(Long packageId) {
        ServicePackage servicePackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Service package not found with id: " + packageId));
        return mapToResponse(servicePackage);
    }

    @Override
    public List<PackageResponse> getAllPackages(boolean onlyActive) {
        List<ServicePackage> list = onlyActive ?
                packageRepository.findByActive(true) :
                packageRepository.findAll();

        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PackageResponse enablePackage(Long packageId) {
        ServicePackage servicePackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Service package not found with id: " + packageId));
        servicePackage.setActive(true);
        return mapToResponse(packageRepository.save(servicePackage));
    }

    @Override
    @Transactional
    public PackageResponse disablePackage(Long packageId) {
        ServicePackage servicePackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Service package not found with id: " + packageId));
        servicePackage.setActive(false);
        return mapToResponse(packageRepository.save(servicePackage));
    }

    private PackageResponse mapToResponse(ServicePackage p) {
        return PackageResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .price(p.getPrice())
                .estimatedDurationMinutes(p.getEstimatedDurationMinutes())
                .features(p.getFeatures())
                .active(p.isActive())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
