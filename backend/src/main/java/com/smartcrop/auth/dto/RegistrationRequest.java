package com.smartcrop.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank(message = "Name is required") @Size(max = 100, message = "Name must not exceed 100 characters") String name,

        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") @Size(max = 255, message = "Email must not exceed 255 characters") String email,

        @NotBlank(message = "Password is required") @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters") String password,

        @Pattern(regexp = "^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$", message = "Invalid phone number")
        @Size(max = 20, message = "Phone must not exceed 20 characters") String phone,

        String language) {
}
