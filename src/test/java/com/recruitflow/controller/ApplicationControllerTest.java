package com.recruitflow.controller;

import com.recruitflow.AbstractIntegrationTest;
import com.recruitflow.model.Job;
import com.recruitflow.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplicationControllerTest extends AbstractIntegrationTest {

    @Autowired
    private JobRepository jobRepository;

    @Test
    void candidateCanApplyToJob() throws Exception {
        Job job = jobRepository.save(new Job("QA Engineer", "Testing", "Engineering", 1));
        String token = tokenFor("candidate", "candidate");
        String body = "{\"jobId\":\"" + job.getId() + "\"}";

        mockMvc.perform(post("/api/applications")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.candidateId").value("candidate"))
                .andExpect(jsonPath("$.stage").value("APPLIED"))
                .andExpect(jsonPath("$.department").value("Engineering"));
    }

    @Test
    void applyingToMissingJobIs404() throws Exception {
        String token = tokenFor("candidate", "candidate");
        String body = "{\"jobId\":\"000000000000000000000000\"}";
        mockMvc.perform(post("/api/applications")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
    }
}
