package com.recruitflow.controller;

import com.recruitflow.dto.request.ApplicationCreateRequest;
import com.recruitflow.dto.request.ApplicationStageRequest;
import com.recruitflow.dto.response.ApplicationResponse;
import com.recruitflow.model.Application;
import com.recruitflow.security.CustomUserPrincipal;
import com.recruitflow.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse apply(@Valid @RequestBody ApplicationCreateRequest request,
                                     @AuthenticationPrincipal CustomUserPrincipal principal) {
        Application app = applicationService.apply(principal.getUsername(), request.jobId());
        return toResponse(app);
    }

    /** Stage change guarded by @deptSecurity: only the owning department's manager. */
    @PatchMapping("/{appId}/stage")
    public ApplicationResponse updateStage(@PathVariable String appId,
                                           @Valid @RequestBody ApplicationStageRequest request) {
        return toResponse(applicationService.updateStage(appId, request));
    }

    @GetMapping("/{appId}")
    public ApplicationResponse get(@PathVariable String appId) {
        return toResponse(applicationService.findById(appId));
    }

    private ApplicationResponse toResponse(Application app) {
        return new ApplicationResponse(app.getId(), app.getCandidateId(), app.getJobId(),
                app.getStage().name(), app.getDepartment(), app.getUpdatedBy(), app.getUpdatedAt());
    }
}
