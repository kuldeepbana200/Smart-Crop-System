package com.smartcrop.intervention.repository;

import com.smartcrop.intervention.entity.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterventionRepository extends JpaRepository<Intervention, Long> {

    List<Intervention> findByDistressAlertIdOrderByCreatedAtDesc(Long distressAlertId);
}
