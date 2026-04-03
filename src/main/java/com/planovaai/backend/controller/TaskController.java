package com.planovaai.backend.controller;

import com.planovaai.backend.entity.Task;
import com.planovaai.backend.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/project/{projectId}")
    public Task createTask(
            @PathVariable Long projectId,
            @RequestBody Task task
            ) {
        return taskService.createTask(projectId, task);
    }

    @GetMapping("/project/{projectId}")
    public List<Task> getTasksByProjectId(@PathVariable Long projectId) {
        return taskService.getTasksByProjectId(projectId);
    }
}
