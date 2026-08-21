package com.recruitflow.repository;

import com.recruitflow.model.CandidateProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CandidateRepository extends MongoRepository<CandidateProfile, String> {
    Optional<CandidateProfile> findByUserId(String userId);

    void deleteByUserId(String userId);
}
