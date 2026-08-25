package com.smartcrop.intervention.repository;

import com.smartcrop.intervention.entity.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import com.smartcrop.intervention.entity.InterventionStatus;

public interface InterventionRepository extends JpaRepository<Intervention, Long> {

    List<Intervention> findByDistressAlertIdOrderByCreatedAtDesc(Long distressAlertId);

    long countByDistressAlertFarmerIdAndStatusIn(
            Long farmerId, List<InterventionStatus> statuses);

    long countByStatus(InterventionStatus status);

    long countByStatusIn(List<InterventionStatus> statuses);

    List<Intervention> findTop5ByStatusOrderByCreatedAtDesc(InterventionStatus status);

    List<Intervention> findTop5ByStatusInOrderByCreatedAtDesc(List<InterventionStatus> statuses);
}
