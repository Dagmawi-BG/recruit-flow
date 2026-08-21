package com.recruitflow.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "candidate_profiles")
public class CandidateProfile {

    @Id
    private String id;

    private String userId;
    private String name;
    private String bio;
    private List<String> skills;
    private int yearsExperience;

    public CandidateProfile() {
    }

    public CandidateProfile(String name, String bio, List<String> skills, int yearsExperience) {
        this.name = name;
        this.bio = bio;
        this.skills = skills;
        this.yearsExperience = yearsExperience;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public int getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(int yearsExperience) {
        this.yearsExperience = yearsExperience;
    }
}
