package com.devtrack.service;

import com.devtrack.dto.TaskRequest;
import com.devtrack.dto.TaskResponse;
import com.devtrack.exception.ResourceNotFoundException;
import com.devtrack.model.Task;
import com.devtrack.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskResponse create(TaskRequest request, String owner) {
        Task task = new Task(
                request.getTitle(),
                request.getDescription(),
                request.getStatus() != null ? request.getStatus() : Task.TaskStatus.TODO,
                owner
        );
        return TaskResponse.from(taskRepository.save(task));
    }

    public List<TaskResponse> listForOwner(String owner) {
        return taskRepository.findByOwnerUsername(owner).stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse update(Long id, TaskRequest request, String requester) {
        Task task = getOwnedTask(id, requester);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        return TaskResponse.from(taskRepository.save(task));
    }

    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found: " + id);
        }
        taskRepository.deleteById(id);
    }

    private Task getOwnedTask(Long id, String requester) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        if (!task.getOwnerUsername().equals(requester)) {
            throw new ResourceNotFoundException("Task not found: " + id);
        }
        return task;
    }
}
