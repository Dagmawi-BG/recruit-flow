package com.recruitflow.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record JobCreateRequest(
        @NotBlank String title,
        String description,
        @NotBlank String department,
        @Min(0) int minExperience
) {
}
