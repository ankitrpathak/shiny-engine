package com.carwash.bookingservice.service;

import com.carwash.bookingservice.dto.PackageRequest;
import com.carwash.bookingservice.dto.PackageResponse;

import java.util.List;

public interface PackageService {
    PackageResponse createPackage(PackageRequest request);
    PackageResponse updatePackage(Long packageId, PackageRequest request);
    PackageResponse getPackageById(Long packageId);
    List<PackageResponse> getAllPackages(boolean onlyActive);
    PackageResponse enablePackage(Long packageId);
    PackageResponse disablePackage(Long packageId);
}
