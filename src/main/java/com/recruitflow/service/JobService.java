package com.recruitflow.service;

import com.recruitflow.dto.request.JobCreateRequest;
import com.recruitflow.exception.ResourceNotFoundException;
import com.recruitflow.model.Job;
import com.recruitflow.repository.JobRepository;
import com.recruitflow.security.annotation.CanEditJob;
import com.recruitflow.security.annotation.IsRecruiter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @IsRecruiter
    public Job create(JobCreateRequest request) {
        // createdBy is populated automatically by MongoDB auditing (@CreatedBy).
        return jobRepository.save(new Job(
                request.title(), request.description(), request.department(), request.minExperience()));
    }

    /** Only the recruiter who created the job (or an admin) may edit it. */
    @CanEditJob
    public Job update(String id, JobCreateRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        job.setTitle(request.title());
        job.setDescription(request.description());
        job.setDepartment(request.department());
        job.setMinExperience(request.minExperience());
        return jobRepository.save(job);
    }

    /** Only the recruiter who created the job (or an admin) may delete it. */
    @CanEditJob
    public void delete(String id) {
        if (!jobRepository.existsById(id)) {
            throw new ResourceNotFoundException("Job not found: " + id);
        }
        jobRepository.deleteById(id);
    }

    public List<Job> findAll() {
        return jobRepository.findAll();
    }

    public Job findById(String id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
    }
}
