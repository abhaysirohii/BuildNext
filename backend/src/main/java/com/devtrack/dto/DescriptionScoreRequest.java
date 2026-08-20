package com.devtrack.dto;

import jakarta.validation.constraints.NotBlank;

public class DescriptionScoreRequest {

    @NotBlank
    private String projectDescription;

    private String jobDescription;

    public String getProjectDescription() { return projectDescription; }
    public void setProjectDescription(String projectDescription) { this.projectDescription = projectDescription; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }
}
