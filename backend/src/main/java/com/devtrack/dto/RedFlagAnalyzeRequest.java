package com.devtrack.dto;

import jakarta.validation.constraints.NotBlank;

public class RedFlagAnalyzeRequest {

    @NotBlank
    private String jobDescription;

    @NotBlank
    private String projectDescription;

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public String getProjectDescription() { return projectDescription; }
    public void setProjectDescription(String projectDescription) { this.projectDescription = projectDescription; }
}
