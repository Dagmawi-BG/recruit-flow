package com.recruitflow.dto.response;

import java.time.Instant;

public record ApplicationResponse(
        String id,
        String candidateId,
        String jobId,
        String stage,
        String department,
        String updatedBy,
        Instant updatedAt
) {
}
