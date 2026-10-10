package com.carwash.adminservice.repository;

import com.carwash.adminservice.entity.AdminAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminActionRepository extends JpaRepository<AdminAction, Long> {

    List<AdminAction> findAllByOrderByPerformedAtDesc();

    List<AdminAction> findByAdminIdOrderByPerformedAtDesc(Long adminId);

    List<AdminAction> findByTargetResourceOrderByPerformedAtDesc(String targetResource);
}
