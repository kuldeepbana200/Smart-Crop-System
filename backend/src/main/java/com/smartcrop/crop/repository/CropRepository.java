package com.smartcrop.crop.repository;

import com.smartcrop.crop.entity.Crop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CropRepository extends JpaRepository<Crop, Long> {

    List<Crop> findByFarmerId(Long farmerId);

    List<Crop> findTop5ByFarmerIdOrderByCreatedAtDesc(Long farmerId);

    long countByFarmerId(Long farmerId);

    Optional<Crop> findByIdAndFarmerId(Long id, Long farmerId);
}