package com.recruitflow.controller;

import com.recruitflow.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JobControllerTest extends AbstractIntegrationTest {

    private static final String JOB_BODY =
            "{\"title\":\"Backend Engineer\",\"description\":\"APIs\",\"department\":\"Engineering\",\"minExperience\":3}";

    @Test
    void candidateCannotCreateJob() throws Exception {
        String token = tokenFor("candidate", "candidate");
        mockMvc.perform(post("/api/jobs")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(APPLICATION_JSON).content(JOB_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void recruiterCanCreateJob() throws Exception {
        String token = tokenFor("recruiter", "recruiter");
        mockMvc.perform(post("/api/jobs")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(APPLICATION_JSON).content(JOB_BODY))
                .andExpect(status().isCreated());
    }

    @Test
    void invalidJobPayloadIs400() throws Exception {
        String token = tokenFor("recruiter", "recruiter");
        String bad = "{\"title\":\"\",\"department\":\"\",\"minExperience\":-1}";
        mockMvc.perform(post("/api/jobs")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(APPLICATION_JSON).content(bad))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creatorCanEditOwnJob() throws Exception {
        String recruiter = tokenFor("recruiter", "recruiter");
        String jobId = createJobAs(recruiter);
        mockMvc.perform(put("/api/jobs/" + jobId)
                        .header(AUTHORIZATION, "Bearer " + recruiter)
                        .contentType(APPLICATION_JSON).content(JOB_BODY))
                .andExpect(status().isOk());
    }

    @Test
    void otherRecruiterCannotEditJob() throws Exception {
        String recruiter = tokenFor("recruiter", "recruiter");
        String jobId = createJobAs(recruiter);
        String otherRecruiter = tokenFor("recruiter2", "recruiter2");
        mockMvc.perform(put("/api/jobs/" + jobId)
                        .header(AUTHORIZATION, "Bearer " + otherRecruiter)
                        .contentType(APPLICATION_JSON).content(JOB_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanEditAnyJob() throws Exception {
        String recruiter = tokenFor("recruiter", "recruiter");
        String jobId = createJobAs(recruiter);
        String admin = tokenFor("admin", "admin");
        mockMvc.perform(put("/api/jobs/" + jobId)
                        .header(AUTHORIZATION, "Bearer " + admin)
                        .contentType(APPLICATION_JSON).content(JOB_BODY))
                .andExpect(status().isOk());
    }

    private String createJobAs(String token) throws Exception {
        String json = mockMvc.perform(post("/api/jobs")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(APPLICATION_JSON).content(JOB_BODY))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asText();
    }
}
