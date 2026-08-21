package com.recruitflow.controller;

import com.recruitflow.dto.request.ProfileUpdateRequest;
import com.recruitflow.dto.response.CandidateResponse;
import com.recruitflow.model.CandidateProfile;
import com.recruitflow.service.CandidateProfileService;
import com.recruitflow.service.CandidateSearchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateSearchService searchService;
    private final CandidateProfileService profileService;

    public CandidateController(CandidateSearchService searchService,
                              CandidateProfileService profileService) {
        this.searchService = searchService;
        this.profileService = profileService;
    }

    /** Autocomplete search: GET /api/candidates/search?q=mar -> Mark, Marcus. */
    @GetMapping("/search")
    public List<CandidateResponse> search(
            @RequestParam("q") String q,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return searchService.autocompleteByName(q, limit);
    }

    /** Candidates may fetch only their own profile; recruiters any (enforced in service). */
    @GetMapping("/{userId}")
    public CandidateProfile getProfile(@PathVariable String userId) {
        return profileService.getByUserId(userId);
    }

    @PutMapping("/{userId}")
    public CandidateProfile updateProfile(@PathVariable String userId,
                                          @Valid @RequestBody ProfileUpdateRequest request) {
        return profileService.updateProfile(userId, request);
    }
}
