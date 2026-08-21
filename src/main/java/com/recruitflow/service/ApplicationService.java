package com.recruitflow.service;

import com.recruitflow.dto.request.ApplicationStageRequest;
import com.recruitflow.exception.ResourceNotFoundException;
import com.recruitflow.model.Application;
import com.recruitflow.model.ApplicationStage;
import com.recruitflow.model.Job;
import com.recruitflow.repository.ApplicationRepository;
import com.recruitflow.repository.JobRepository;
import com.recruitflow.security.annotation.IsHiringManagerForApplication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    public ApplicationService(ApplicationRepository applicationRepository, JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    public Application apply(String candidateUsername, String jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));
        Application application = new Application(candidateUsername, jobId,
                ApplicationStage.APPLIED, job.getDepartment());
        return applicationRepository.save(application);
    }

    /**
     * Only a hiring manager of the application's own department may advance its stage.
     */
    @IsHiringManagerForApplication
    public Application updateStage(String appId, ApplicationStageRequest request) {
        Application application = applicationRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + appId));
        application.setStage(request.stage());
        return applicationRepository.save(application);
    }

    public List<Application> findByCandidate(String candidateUsername) {
        return applicationRepository.findByCandidateId(candidateUsername);
    }

    public Application findById(String appId) {
        return applicationRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + appId));
    }
}
