package com.recruitflow.controller;

import com.recruitflow.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends AbstractIntegrationTest {

    @Test
    void unauthenticatedRequestToProtectedEndpointIs401() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validLoginReturnsJwt() throws Exception {
        String body = "{\"username\":\"recruiter\",\"password\":\"recruiter\"}";
        mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.role").value("RECRUITER"));
    }

    @Test
    void invalidCredentialsAre401() throws Exception {
        String body = "{\"username\":\"recruiter\",\"password\":\"wrong\"}";
        mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void blankCredentialsAre400() throws Exception {
        String body = "{\"username\":\"\",\"password\":\"\"}";
        mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerCreatesCandidateAndReturnsToken() throws Exception {
        String username = "newcand_" + System.currentTimeMillis();
        String body = "{\"username\":\"" + username + "\",\"password\":\"secret123\"}";
        mockMvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.role").value("CANDIDATE"));

        // The new account can log in.
        mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    @Test
    void registerDuplicateUsernameIs409() throws Exception {
        String body = "{\"username\":\"recruiter\",\"password\":\"secret123\"}";
        mockMvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void registerInvalidPayloadIs400() throws Exception {
        String body = "{\"username\":\"ab\",\"password\":\"123\"}";
        mockMvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshIssuesNewTokens() throws Exception {
        String refreshToken = loginAndGetRefreshToken("recruiter", "recruiter");
        String body = "{\"refreshToken\":\"" + refreshToken + "\"}";
        mockMvc.perform(post("/api/auth/refresh").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()));
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        String refreshToken = loginAndGetRefreshToken("candidate", "candidate");
        String body = "{\"refreshToken\":\"" + refreshToken + "\"}";
        mockMvc.perform(post("/api/auth/logout").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isNoContent());
        // The revoked token can no longer be refreshed.
        mockMvc.perform(post("/api/auth/refresh").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshWithUnknownTokenIs401() throws Exception {
        String body = "{\"refreshToken\":\"does-not-exist\"}";
        mockMvc.perform(post("/api/auth/refresh").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    private String loginAndGetRefreshToken(String username, String password) throws Exception {
        String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        String json = mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("refreshToken").asText();
    }
}
