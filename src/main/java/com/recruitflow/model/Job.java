package com.recruitflow.model;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "jobs")
public class Job {

    @Id
    private String id;

    private String title;
    private String description;
    private String department;
    private int minExperience;

    /** Set automatically by MongoDB auditing to the username of the creator. */
    @CreatedBy
    private String createdBy;
    @CreatedDate
    private Instant createdAt;

    public Job() {
    }

    public Job(String title, String description, String department, int minExperience) {
        this.title = title;
        this.description = description;
        this.department = department;
        this.minExperience = minExperience;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getMinExperience() {
        return minExperience;
    }

    public void setMinExperience(int minExperience) {
        this.minExperience = minExperience;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
