package com.carwash.bookingservice.controller;

import com.carwash.bookingservice.dto.PackageRequest;
import com.carwash.bookingservice.dto.PackageResponse;
import com.carwash.bookingservice.service.PackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
@Tag(name = "Packages", description = "Service Packages Management")
public class PackageController {

    private final PackageService packageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Create a new service package (Admin only)")
    public ResponseEntity<PackageResponse> createPackage(@Valid @RequestBody PackageRequest request) {
        return new ResponseEntity<>(packageService.createPackage(request), HttpStatus.CREATED);
    }

    @PutMapping("/{packageId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Update an existing service package (Admin only)")
    public ResponseEntity<PackageResponse> updatePackage(
            @PathVariable Long packageId,
            @Valid @RequestBody PackageRequest request) {
        return ResponseEntity.ok(packageService.updatePackage(packageId, request));
    }

    @GetMapping("/{packageId}")
    @Operation(summary = "Get service package details by ID")
    public ResponseEntity<PackageResponse> getPackageById(@PathVariable Long packageId) {
        return ResponseEntity.ok(packageService.getPackageById(packageId));
    }

    @GetMapping
    @Operation(summary = "List all active service packages (or all if active=false query param)")
    public ResponseEntity<List<PackageResponse>> getAllPackages(
            @RequestParam(required = false, defaultValue = "true") boolean active) {
        return ResponseEntity.ok(packageService.getAllPackages(active));
    }

    @PutMapping("/{packageId}/enable")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Enable a service package (Admin only)")
    public ResponseEntity<PackageResponse> enablePackage(@PathVariable Long packageId) {
        return ResponseEntity.ok(packageService.enablePackage(packageId));
    }

    @PutMapping("/{packageId}/disable")
    @PreAuthorize("hasAnyRole('ADMIN', 'GOD')")
    @Operation(summary = "Disable a service package (Admin only)")
    public ResponseEntity<PackageResponse> disablePackage(@PathVariable Long packageId) {
        return ResponseEntity.ok(packageService.disablePackage(packageId));
    }
}
