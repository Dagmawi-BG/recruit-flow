package com.recruitflow.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CandidateSearchRequest(
        @NotBlank String query,
        @Min(0) int minExperience
) {
}
