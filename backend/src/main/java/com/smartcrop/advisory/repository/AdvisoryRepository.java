package com.smartcrop.advisory.repository;

import com.smartcrop.advisory.entity.Advisory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdvisoryRepository extends JpaRepository<Advisory, Long> {

    List<Advisory> findByCropFarmerIdOrderByGeneratedAtDesc(Long farmerId);

    Optional<Advisory> findByIdAndCropFarmerId(Long id, Long farmerId);
}
