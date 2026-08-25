package com.smartcrop.advisory.repository;

import com.smartcrop.advisory.entity.AdvisoryRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvisoryRecommendationRepository extends JpaRepository<AdvisoryRecommendation, Long> {
}
