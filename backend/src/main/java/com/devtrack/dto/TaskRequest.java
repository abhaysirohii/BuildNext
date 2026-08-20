package com.devtrack.dto;

import com.devtrack.model.Task;
import jakarta.validation.constraints.NotBlank;

public class TaskRequest {

    @NotBlank
    private String title;

    private String description;

    private Task.TaskStatus status;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Task.TaskStatus getStatus() { return status; }
    public void setStatus(Task.TaskStatus status) { this.status = status; }
}
