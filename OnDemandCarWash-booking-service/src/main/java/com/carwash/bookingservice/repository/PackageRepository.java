package com.carwash.bookingservice.repository;

import com.carwash.bookingservice.entity.ServicePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<ServicePackage, Long> {
    List<ServicePackage> findByActive(boolean active);
}
