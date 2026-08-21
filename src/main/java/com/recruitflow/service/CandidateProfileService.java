package com.recruitflow.service;

import com.recruitflow.dto.request.ProfileUpdateRequest;
import com.recruitflow.exception.ResourceNotFoundException;
import com.recruitflow.model.CandidateProfile;
import com.recruitflow.repository.CandidateRepository;
import com.recruitflow.security.annotation.CanViewProfile;
import com.recruitflow.security.annotation.IsProfileOwner;
import org.springframework.stereotype.Service;

@Service
public class CandidateProfileService {

    private final CandidateRepository candidateRepository;

    public CandidateProfileService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    /**
     * Candidates may only read their own profile; recruiters may read any.
     */
    @CanViewProfile
    public CandidateProfile getByUserId(String userId) {
        return candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found: " + userId));
    }

    /** Only the profile's owner may update it (recruiters can read, not edit). */
    @IsProfileOwner
    public CandidateProfile updateProfile(String userId, ProfileUpdateRequest request) {
        CandidateProfile profile = candidateRepository.findByUserId(userId)
                .orElseGet(() -> {
                    CandidateProfile p = new CandidateProfile();
                    p.setUserId(userId);
                    return p;
                });
        profile.setName(request.name());
        profile.setBio(request.bio());
        profile.setSkills(request.skills());
        profile.setYearsExperience(request.yearsExperience());
        return candidateRepository.save(profile);
    }
}
