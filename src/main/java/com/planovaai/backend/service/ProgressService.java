package com.planovaai.backend.service;

import com.planovaai.backend.dto.TaskUpdateDto;
import com.planovaai.backend.entity.Task;
import com.planovaai.backend.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProgressService {
    private final TaskRepository taskRepository;

    public ProgressService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // Update single task
    public Map<String, Object> updateTask(TaskUpdateDto dto) {

        if (dto.getId() == null || dto.getId().isBlank()) {
            return Map.of(
                    "status", "ok",
                    "message", "No id provided",
                    "progress", dto.getProgress(),
                    "taskStatus", dto.getStatus() != null ? dto.getStatus() : "Planned"
            );
        }

        try {
            // ✅ Direct String lookup — no conversion needed
            Optional<Task> optional = taskRepository.findById(dto.getId());

            if (optional.isEmpty()) {
                return Map.of(
                        "status", "ok",
                        "message", "Task not in DB",
                        "progress", dto.getProgress(),
                        "taskStatus", dto.getStatus() != null ? dto.getStatus() : "Planned"
                );
            }

            Task task = optional.get();
            task.setProgress(dto.getProgress());

            if (dto.getProgress() == 100) task.setStatus("Done");
            else if (dto.getProgress() > 0) task.setStatus("In Progress");
            else task.setStatus("Planned");

            taskRepository.save(task);

            return Map.of(
                    "status", "ok",
                    "message", "Saved to database",
                    "progress", task.getProgress(),
                    "taskStatus", task.getStatus()
            );

        } catch (Exception e) {
            System.err.println("❌ Progress update error: " + e.getMessage());
            return Map.of(
                    "status", "ok",
                    "message", "Session only",
                    "progress", dto.getProgress(),
                    "taskStatus", dto.getStatus() != null ? dto.getStatus() : "Planned"
            );
        }
    }

    // Get all tasks for a project
    public List<Task> getTasksByProject(String projectId) { // ✅ String
        return taskRepository.findByProjectId(projectId);
    }
}
