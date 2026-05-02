package com.planovaai.backend.controller;

import com.planovaai.backend.dto.TaskUpdateDto;
import com.planovaai.backend.entity.Project;
import com.planovaai.backend.entity.Task;
import com.planovaai.backend.repository.ProjectRepository;
import com.planovaai.backend.repository.TaskRepository;
import com.planovaai.backend.service.JwtService;
import com.planovaai.backend.service.ProgressService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "*")
public class ProgressController {
    private final ProgressService progressService;
    private final JwtService jwtService;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public ProgressController(
            ProgressService progressService,
            JwtService jwtService,
            ProjectRepository projectRepository,
            TaskRepository taskRepository
    ) {
        this.progressService = progressService;
        this.jwtService = jwtService;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    // Update single task progress
    @PutMapping("/update")
    public ResponseEntity<?> updateTask(@RequestBody TaskUpdateDto dto) {
        Map<String, Object> result = progressService.updateTask(dto); // ✅ Map not Task
        return ResponseEntity.ok(result);
    }

    @GetMapping("/tasks/{projectId}")
    public ResponseEntity<?> getTasks(@PathVariable String projectId) {
        List<Task> tasks = progressService.getTasksByProject(projectId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<?> getMyTasks(HttpServletRequest request) {
        try {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            String userId = jwtService.extractUserId(header.substring(7));

            // Get latest project for this user
            List<Project> projects = projectRepository.findByUserIdOrderByIdDesc(userId);
            if (projects.isEmpty()) {
                return ResponseEntity.ok(Map.of("tasks", List.of(), "model", ""));
            }

            Project latest = projects.get(0);
            List<Task> tasks = taskRepository.findByProjectId(latest.getId());

            // Convert to map for frontend
            List<Map<String, Object>> taskMaps = tasks.stream().map(t -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", t.getId().toString());
                map.put("title", t.getTitle());
                map.put("description", t.getDescription() != null ? t.getDescription() : "");
                map.put("startDate", t.getStartDate());
                map.put("endDate", t.getEndDate());
                map.put("duration", t.getDuration());
                map.put("status", t.getStatus());
                map.put("progress", t.getProgress());
                map.put("type", t.getType() != null ? t.getType() : "FR");
                map.put("dependsOn", t.getDependsOn());
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "projectId", latest.getId(),
                    "projectName", latest.getName(),
                    "model", latest.getDescription(),
                    "tasks", taskMaps
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
