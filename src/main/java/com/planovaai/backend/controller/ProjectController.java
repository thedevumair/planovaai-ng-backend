package com.planovaai.backend.controller;

import com.planovaai.backend.entity.Project;
import com.planovaai.backend.service.ProjectService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/user/{userId}")
    public Project createProject(
            @PathVariable Long userId,
            @RequestBody Project project
    ) {
        return projectService.createProject(userId, project);
    }
}
