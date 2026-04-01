package com.planovaai.backend.service;

import com.planovaai.backend.entity.Project;
import com.planovaai.backend.entity.Task;
import com.planovaai.backend.repository.ProjectRepository;
import com.planovaai.backend.repository.TaskRepository;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    public Task createTask(Long projectId, Task task) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        task.setProject(project);
        return taskRepository.save(task);
    }
}
