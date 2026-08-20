package com.devtrack.service;

import com.devtrack.dto.TaskRequest;
import com.devtrack.dto.TaskResponse;
import com.devtrack.exception.ResourceNotFoundException;
import com.devtrack.model.Task;
import com.devtrack.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task existingTask;

    @BeforeEach
    void setUp() {
        existingTask = new Task("Old title", "Old desc", Task.TaskStatus.TODO, "alice");
        existingTask.setId(1L);
        existingTask.setCreatedAt(Instant.now());
    }

    @Test
    void create_savesTaskOwnedByCaller() {
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskRequest req = new TaskRequest();
        req.setTitle("New task");

        TaskResponse response = taskService.create(req, "alice");

        assertEquals("New task", response.getTitle());
        assertEquals("alice", response.getOwnerUsername());
        assertEquals(Task.TaskStatus.TODO, response.getStatus());
    }

    @Test
    void update_throwsWhenRequesterIsNotOwner() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));

        TaskRequest req = new TaskRequest();
        req.setTitle("Hacked title");

        assertThrows(ResourceNotFoundException.class, () -> taskService.update(1L, req, "mallory"));
    }

    @Test
    void update_succeedsForOwner() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskRequest req = new TaskRequest();
        req.setTitle("Updated title");
        req.setStatus(Task.TaskStatus.DONE);

        TaskResponse response = taskService.update(1L, req, "alice");

        assertEquals("Updated title", response.getTitle());
        assertEquals(Task.TaskStatus.DONE, response.getStatus());
    }

    @Test
    void listForOwner_returnsOnlyThatOwnersTasks() {
        when(taskRepository.findByOwnerUsername("alice")).thenReturn(List.of(existingTask));

        List<TaskResponse> results = taskService.listForOwner("alice");

        assertEquals(1, results.size());
        assertEquals("alice", results.get(0).getOwnerUsername());
    }
}
