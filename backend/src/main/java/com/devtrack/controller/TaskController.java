package com.devtrack.controller;

import com.devtrack.dto.TaskRequest;
import com.devtrack.dto.TaskResponse;
import com.devtrack.service.TaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request, Authentication auth) {
        return ResponseEntity.ok(taskService.create(request, auth.getName()));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> list(Authentication auth) {
        return ResponseEntity.ok(taskService.listForOwner(auth.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id, @Valid @RequestBody TaskRequest request,
                                                Authentication auth) {
        return ResponseEntity.ok(taskService.update(id, request, auth.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
