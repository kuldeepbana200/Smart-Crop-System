package com.smartcrop.education.controller;

import com.smartcrop.education.dto.EducationResourceDTO;
import com.smartcrop.education.service.EducationResourceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/education/resources")
public class EducationResourceController {

    private final EducationResourceService educationResourceService;

    public EducationResourceController(EducationResourceService educationResourceService) {
        this.educationResourceService = educationResourceService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EducationResourceDTO> createEducationResource(
            @Valid @RequestBody EducationResourceDTO dto) {
        EducationResourceDTO created = educationResourceService.createEducationResource(dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EducationResourceDTO> updateEducationResource(
            @PathVariable Long id,
            @Valid @RequestBody EducationResourceDTO dto) {
        EducationResourceDTO updated = educationResourceService.updateEducationResource(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEducationResource(@PathVariable Long id) {
        educationResourceService.deleteEducationResource(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FARMER', 'ADMIN')")
    public ResponseEntity<List<EducationResourceDTO>> getEducationResources(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String category) {
        List<EducationResourceDTO> resources = educationResourceService.getEducationResources(language, category);
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FARMER', 'ADMIN')")
    public ResponseEntity<EducationResourceDTO> getEducationResourceById(
            @PathVariable Long id,
            @RequestParam(required = false) String language) {
        EducationResourceDTO resource = educationResourceService.getEducationResourceById(id, language);
        return ResponseEntity.ok(resource);
    }
}