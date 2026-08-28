package com.smartcrop.education.service;

import com.smartcrop.education.dto.EducationResourceDTO;
import com.smartcrop.education.entity.EducationResource;
import com.smartcrop.education.repository.EducationResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EducationResourceService {

    private final EducationResourceRepository educationResourceRepository;

    public EducationResourceService(EducationResourceRepository educationResourceRepository) {
        this.educationResourceRepository = educationResourceRepository;
    }

    @Transactional
    public EducationResourceDTO createEducationResource(EducationResourceDTO dto) {
        EducationResource resource = new EducationResource(
                null,
                dto.title(),
                dto.content(),
                dto.category(),
                dto.externalUrl(),
                dto.language(),
                null,
                null
        );
        EducationResource saved = educationResourceRepository.save(resource);
        return toDto(saved);
    }

    @Transactional
    public EducationResourceDTO updateEducationResource(Long id, EducationResourceDTO dto) {
        EducationResource resource = educationResourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Education resource not found"));
        resource.setTitle(dto.title());
        resource.setContent(dto.content());
        resource.setCategory(dto.category());
        resource.setExternalUrl(dto.externalUrl());
        resource.setLanguage(dto.language());
        // updatedAt will be updated by @PreUpdate
        EducationResource saved = educationResourceRepository.save(resource);
        return toDto(saved);
    }

    @Transactional
    public void deleteEducationResource(Long id) {
        if (!educationResourceRepository.existsById(id)) {
            throw new RuntimeException("Education resource not found");
        }
        educationResourceRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<EducationResourceDTO> getEducationResources(String language, String category) {
        String lang = (language == null || language.isBlank()) ? "en" : language.toLowerCase();
        List<EducationResource> resources;

        if (category == null || category.isBlank()) {
            resources = educationResourceRepository.findByLanguageOrderByCreatedAtDesc(lang);
        } else {
            resources = educationResourceRepository.findByLanguageAndCategoryOrderByCreatedAtDesc(lang, category);
        }

        // Fallback to English if no resources found in the requested language and language is not English
        if (resources.isEmpty() && !lang.equals("en")) {
            if (category == null || category.isBlank()) {
                resources = educationResourceRepository.findByLanguageOrderByCreatedAtDesc("en");
            } else {
                resources = educationResourceRepository.findByLanguageAndCategoryOrderByCreatedAtDesc("en", category);
            }
        }

        return resources.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EducationResourceDTO getEducationResourceById(Long id, String language) {
        String lang = (language == null || language.isBlank()) ? "en" : language.toLowerCase();
        EducationResource resource = educationResourceRepository
                .findByIdAndLanguage(id, lang)
                .or(() -> {
                    if (lang.equals("en")) {
                        return Optional.empty();
                    }
                    return educationResourceRepository.findByIdAndLanguage(id, "en");
                })
                .orElseThrow(() -> new RuntimeException("Education resource not found"));

        return toDto(resource);
    }

    private EducationResourceDTO toDto(EducationResource resource) {
        return new EducationResourceDTO(
                resource.getTitle(),
                resource.getContent(),
                resource.getCategory(),
                resource.getExternalUrl(),
                resource.getLanguage()
        );
    }
}