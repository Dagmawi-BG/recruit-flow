package com.recruitflow.dto.request;

import com.recruitflow.model.ApplicationStage;
import jakarta.validation.constraints.NotNull;

public record ApplicationStageRequest(
        @NotNull ApplicationStage stage
) {
}
