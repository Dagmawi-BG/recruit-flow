package com.recruitflow.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ApplicationCreateRequest(
        @NotBlank String jobId
) {
}
