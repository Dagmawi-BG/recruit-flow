package com.recruitflow.dto.response;

import java.util.List;

public record CandidateResponse(
        String id,
        String name,
        String bio,
        List<String> skills,
        int yearsExperience,
        double score
) {
}
