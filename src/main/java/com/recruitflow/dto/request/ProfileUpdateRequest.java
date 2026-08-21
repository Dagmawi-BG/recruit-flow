package com.recruitflow.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ProfileUpdateRequest(
        @NotBlank String name,
        String bio,
        List<String> skills,
        @Min(0) int yearsExperience
) {
}
