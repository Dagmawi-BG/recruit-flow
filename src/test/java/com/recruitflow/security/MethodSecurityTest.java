package com.recruitflow.security;

import com.recruitflow.AbstractIntegrationTest;
import com.recruitflow.model.Application;
import com.recruitflow.model.ApplicationStage;
import com.recruitflow.model.Job;
import com.recruitflow.repository.ApplicationRepository;
import com.recruitflow.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Department-level custom SpEL security (@deptSecurity.isManagerForApplication).
 */
class MethodSecurityTest extends AbstractIntegrationTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Test
    void managerFromOwnDepartmentCanAdvanceStage() throws Exception {
        Application app = seedEngineeringApplication();
        String token = tokenFor("eng_manager", "eng_manager");
        mockMvc.perform(patch("/api/applications/" + app.getId() + "/stage")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(APPLICATION_JSON).content("{\"stage\":\"SCREEN\"}"))
                .andExpect(status().isOk())
                // Auditing records who advanced the application.
                .andExpect(jsonPath("$.updatedBy").value("eng_manager"));
    }

    @Test
    void managerFromOtherDepartmentIsForbidden() throws Exception {
        Application app = seedEngineeringApplication();
        String token = tokenFor("mkt_manager", "mkt_manager");
        mockMvc.perform(patch("/api/applications/" + app.getId() + "/stage")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(APPLICATION_JSON).content("{\"stage\":\"SCREEN\"}"))
                .andExpect(status().isForbidden());
    }

    private Application seedEngineeringApplication() {
        Job job = jobRepository.save(new Job("Eng Role", "desc", "Engineering", 2));
        return applicationRepository.save(
                new Application("candidate", job.getId(), ApplicationStage.APPLIED, "Engineering"));
    }
}
