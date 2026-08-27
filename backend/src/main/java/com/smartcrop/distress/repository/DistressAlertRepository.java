package com.smartcrop.distress.repository;

import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.distress.entity.DistressAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DistressAlertRepository extends JpaRepository<DistressAlert, Long> {

    List<DistressAlert> findByStatusOrderByCreatedAtDesc(AlertStatus status);

    List<DistressAlert> findTop5ByStatusOrderByCreatedAtDesc(AlertStatus status);

    long countByStatus(AlertStatus status);

    List<DistressAlert> findByAssignedOfficerIdOrderByCreatedAtDesc(Long officerId);

    List<DistressAlert> findByFarmerIdOrderByCreatedAtDesc(Long farmerId);

    List<DistressAlert> findTop5ByFarmerIdOrderByCreatedAtDesc(Long farmerId);

    long countByFarmerIdAndStatus(Long farmerId, AlertStatus status);

    List<DistressAlert> findByFarmerIdAndStatusOrderByCreatedAtDesc(Long farmerId, AlertStatus status);

    Optional<DistressAlert> findByFarmerIdAndCropIdAndConditionKeyAndStatus(
            Long farmerId, Long cropId, String conditionKey, AlertStatus status);

    Optional<DistressAlert> findByIdAndFarmerId(Long id, Long farmerId);
}
