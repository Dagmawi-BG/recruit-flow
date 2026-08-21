package com.recruitflow.controller;

import com.recruitflow.dto.request.JobCreateRequest;
import com.recruitflow.model.Job;
import com.recruitflow.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    /** Only RECRUITER may create (enforced in service via @IsRecruiter). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Job create(@Valid @RequestBody JobCreateRequest request) {
        return jobService.create(request);
    }

    @GetMapping
    public List<Job> list() {
        return jobService.findAll();
    }

    @GetMapping("/{id}")
    public Job get(@PathVariable String id) {
        return jobService.findById(id);
    }

    /** Only the creating recruiter (or an admin) may edit — enforced via @CanEditJob. */
    @PutMapping("/{id}")
    public Job update(@PathVariable String id, @Valid @RequestBody JobCreateRequest request) {
        return jobService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        jobService.delete(id);
    }
}
