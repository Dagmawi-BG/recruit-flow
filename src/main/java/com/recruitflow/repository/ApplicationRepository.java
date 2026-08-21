package com.recruitflow.repository;

import com.recruitflow.model.Application;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ApplicationRepository extends MongoRepository<Application, String> {
    List<Application> findByCandidateId(String candidateId);
}
