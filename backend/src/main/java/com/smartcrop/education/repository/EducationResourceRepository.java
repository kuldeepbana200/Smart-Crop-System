package com.smartcrop.education.repository;

import com.smartcrop.education.entity.EducationResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EducationResourceRepository extends JpaRepository<EducationResource, Long> {

    List<EducationResource> findByLanguageOrderByCreatedAtDesc(String language);

    List<EducationResource> findByLanguageAndCategoryOrderByCreatedAtDesc(String language, String category);

    Optional<EducationResource> findByIdAndLanguage(Long id, String language);
}