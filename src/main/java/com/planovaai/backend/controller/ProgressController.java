package com.planovaai.backend.controller;

import com.planovaai.backend.dto.TaskUpdateDto;
import com.planovaai.backend.entity.Project;
import com.planovaai.backend.entity.Task;
import com.planovaai.backend.entity.TaskAssignment;
import com.planovaai.backend.repository.ProjectRepository;
import com.planovaai.backend.repository.TaskAssignmentRepository;
import com.planovaai.backend.repository.TaskRepository;
import com.planovaai.backend.service.JwtService;
import com.planovaai.backend.service.ProgressService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "*")
public class ProgressController {
    private final ProgressService progressService;
    private final JwtService jwtService;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    public ProgressController(
            ProgressService progressService,
            JwtService jwtService,
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            TaskAssignmentRepository taskAssignmentRepository
    ) {
        this.progressService = progressService;
        this.jwtService = jwtService;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
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

            List<Project> projects = projectRepository.findByUserIdOrderByIdDesc(userId);
            if (projects.isEmpty()) {
                return ResponseEntity.ok(Map.of("tasks", List.of(), "model", ""));
            }

            Project latest = projects.get(0);
            List<Task> tasks = taskRepository
                    .findByProjectIdOrderByStartDateAsc(latest.getId());

            List<Map<String, Object>> taskMaps = tasks.stream().map(t -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", t.getId());
                map.put("title", t.getTitle());
                map.put("description", t.getDescription() != null ? t.getDescription() : "");
                map.put("startDate", t.getStartDate());
                map.put("endDate", t.getEndDate());
                map.put("duration", t.getDuration());
                map.put("type", t.getType() != null ? t.getType() : "FR");
                map.put("dependsOn", t.getDependsOn());

                // Calculate progress from assignments
                List<TaskAssignment> assignments =
                        taskAssignmentRepository.findByTaskId(t.getId());

                if (!assignments.isEmpty()) {
                    // Average of all developer progress
                    int avgDevProgress = assignments.stream()
                            .mapToInt(TaskAssignment::getProgress)
                            .sum() / assignments.size();

                    // Use developer progress if higher than task progress
                    int finalProgress = Math.max(t.getProgress(), avgDevProgress);
                    map.put("progress", finalProgress);

                    // Derive status from progress
                    String status;
                    if (finalProgress == 100) status = "Done";
                    else if (finalProgress > 0) status = "In Progress";
                    else status = t.getStatus();
                    map.put("status", status);

                } else {
                    map.put("progress", t.getProgress());
                    map.put("status", t.getStatus());
                }

                return map;
            }).collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("projectId", latest.getId());
            result.put("projectName", latest.getName());
            result.put("model", latest.getDescription() != null ? latest.getDescription() : "");
            result.put("tasks", taskMaps);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/project-summary/{projectId}")
    public ResponseEntity<?> getProjectSummary(@PathVariable String projectId) {
        try {
            List<Task> tasks = taskRepository
                    .findByProjectIdOrderByStartDateAsc(projectId);

            if (tasks.isEmpty()) {
                return ResponseEntity.ok(Map.of("error", "No tasks found"));
            }

            LocalDate projectStart = tasks.stream()
                    .map(Task::getStartDate).filter(Objects::nonNull)
                    .min(LocalDate::compareTo).orElse(LocalDate.now());

            LocalDate projectEnd = tasks.stream()
                    .map(Task::getEndDate).filter(Objects::nonNull)
                    .max(LocalDate::compareTo).orElse(LocalDate.now());

            LocalDate today = LocalDate.now();

            long totalDays = ChronoUnit.DAYS.between(projectStart, projectEnd);
            long daysElapsed = ChronoUnit.DAYS.between(projectStart, today);
            long daysLeft = ChronoUnit.DAYS.between(today, projectEnd);

            // Calculate progress including developer assignments
            int totalProgress = 0;
            long doneTasks = 0, inProgressTasks = 0, plannedTasks = 0, delayedTasks = 0;

            for (Task t : tasks) {
                List<TaskAssignment> assignments =
                        taskAssignmentRepository.findByTaskId(t.getId());

                int taskProgress = t.getProgress();

                if (!assignments.isEmpty()) {
                    int avgDevProgress = assignments.stream()
                            .mapToInt(TaskAssignment::getProgress)
                            .sum() / assignments.size();
                    taskProgress = Math.max(taskProgress, avgDevProgress);
                }

                totalProgress += taskProgress;

                // Count statuses based on real progress
                if (taskProgress == 100) doneTasks++;
                else if (taskProgress > 0) inProgressTasks++;
                else if (t.getEndDate() != null && t.getEndDate().isBefore(today)) delayedTasks++;
                else plannedTasks++;
            }

            int avgProgress = tasks.isEmpty() ? 0 : totalProgress / tasks.size();

            int expectedProgress = totalDays > 0
                    ? (int) Math.min(100, (daysElapsed * 100) / totalDays) : 0;

            int gap = expectedProgress - avgProgress;
            String health = gap <= 0 ? "ON_TRACK" : gap <= 15 ? "AT_RISK" : "BEHIND";

            LocalDate actualStart = tasks.stream()
                    .filter(t -> !"Planned".equalsIgnoreCase(t.getStatus()))
                    .map(Task::getStartDate).filter(Objects::nonNull)
                    .min(LocalDate::compareTo).orElse(null);

            Map<String, Object> result = new HashMap<>();
            result.put("projectStart", projectStart.toString());
            result.put("projectEnd", projectEnd.toString());
            result.put("actualStart", actualStart != null ? actualStart.toString() : "");
            result.put("totalDays", totalDays);
            result.put("daysElapsed", Math.max(0, daysElapsed));
            result.put("daysLeft", Math.max(0, daysLeft));
            result.put("totalTasks", tasks.size());
            result.put("doneTasks", doneTasks);
            result.put("inProgressTasks", inProgressTasks);
            result.put("plannedTasks", plannedTasks);
            result.put("delayedTasks", delayedTasks);
            result.put("avgProgress", avgProgress);
            result.put("expectedProgress", expectedProgress);
            result.put("health", health);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
