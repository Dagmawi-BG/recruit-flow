package com.recruitflow.controller;

import com.recruitflow.AbstractIntegrationTest;
import com.recruitflow.model.CandidateProfile;
import com.recruitflow.repository.CandidateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CandidateControllerTest extends AbstractIntegrationTest {

    @Autowired
    private CandidateRepository candidateRepository;

    @Test
    void candidateCanReadOwnProfile() throws Exception {
        String token = tokenFor("candidate", "candidate");
        mockMvc.perform(get("/api/candidates/candidate")
                        .header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("candidate"));
    }

    @Test
    void candidateCannotReadAnotherProfile() throws Exception {
        // Seed another candidate's profile directly (recruiters can no longer create via the API).
        // Clear first so repeated runs against the shared test DB stay deterministic.
        candidateRepository.deleteByUserId("otheruser");
        CandidateProfile other = new CandidateProfile("Other Person", "x", List.of("Go"), 4);
        other.setUserId("otheruser");
        candidateRepository.save(other);

        String candidate = tokenFor("candidate", "candidate");
        mockMvc.perform(get("/api/candidates/otheruser")
                        .header(AUTHORIZATION, "Bearer " + candidate))
                .andExpect(status().isForbidden());
    }

    @Test
    void recruiterCanReadAnyProfile() throws Exception {
        String recruiter = tokenFor("recruiter", "recruiter");
        mockMvc.perform(get("/api/candidates/candidate")
                        .header(AUTHORIZATION, "Bearer " + recruiter))
                .andExpect(status().isOk());
    }

    @Test
    void candidateCanUpdateOwnProfile() throws Exception {
        String candidate = tokenFor("candidate", "candidate");
        String profile = "{\"name\":\"Charlie\",\"bio\":\"updated\",\"skills\":[\"Java\"],\"yearsExperience\":3}";
        mockMvc.perform(put("/api/candidates/candidate")
                        .header(AUTHORIZATION, "Bearer " + candidate)
                        .contentType(APPLICATION_JSON).content(profile))
                .andExpect(status().isOk());
    }

    @Test
    void recruiterCannotUpdateAnotherProfile() throws Exception {
        String recruiter = tokenFor("recruiter", "recruiter");
        String profile = "{\"name\":\"Hacked\",\"bio\":\"x\",\"skills\":[\"Go\"],\"yearsExperience\":4}";
        mockMvc.perform(put("/api/candidates/candidate")
                        .header(AUTHORIZATION, "Bearer " + recruiter)
                        .contentType(APPLICATION_JSON).content(profile))
                .andExpect(status().isForbidden());
    }
}
