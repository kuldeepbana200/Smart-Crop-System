package com.smartcrop.farmer.repository;

import com.smartcrop.farmer.entity.Farmer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {

    Optional<Farmer> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Override
    @EntityGraph(attributePaths = {"user"})
    List<Farmer> findAll();
}