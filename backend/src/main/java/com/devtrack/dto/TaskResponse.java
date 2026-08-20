package com.devtrack.dto;

import com.devtrack.model.Task;

import java.time.Instant;

public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private Task.TaskStatus status;
    private Instant createdAt;
    private String ownerUsername;

    public TaskResponse(Long id, String title, String description, Task.TaskStatus status,
                         Instant createdAt, String ownerUsername) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.ownerUsername = ownerUsername;
    }

    public static TaskResponse from(Task t) {
        return new TaskResponse(t.getId(), t.getTitle(), t.getDescription(),
                t.getStatus(), t.getCreatedAt(), t.getOwnerUsername());
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Task.TaskStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public String getOwnerUsername() { return ownerUsername; }
}
