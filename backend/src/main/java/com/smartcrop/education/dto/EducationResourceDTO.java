package com.smartcrop.education.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EducationResourceDTO(

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Content is required")
        String content,

        @NotBlank(message = "Category is required")
        @Size(max = 100, message = "Category must not exceed 100 characters")
        String category,

        String externalUrl,

        @NotBlank(message = "Language is required")
        @Size(min = 2, max = 2, message = "Language must be a 2-letter ISO 639-1 code")
        String language
) {
}