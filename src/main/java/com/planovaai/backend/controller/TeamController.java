package com.planovaai.backend.controller;

import com.planovaai.backend.entity.TaskAssignment;
import com.planovaai.backend.entity.TeamMember;
import com.planovaai.backend.repository.TaskAssignmentRepository;
import com.planovaai.backend.service.JwtService;
import com.planovaai.backend.service.TeamService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/team")
@CrossOrigin(origins = "*")
public class TeamController {

    private final TeamService teamService;
    private final JwtService jwtService;
    private final TaskAssignmentRepository taskAssignmentRepository;

    public TeamController(TeamService teamService,
                          JwtService jwtService,
                          TaskAssignmentRepository taskAssignmentRepository
    ) {
        this.teamService = teamService;
        this.jwtService = jwtService;
        this.taskAssignmentRepository = taskAssignmentRepository;
    }

    // Add member by email
    @PostMapping("/members/email")
    public ResponseEntity<?> addByEmail(@RequestBody Map<String, String> body) {
        try {
            TeamMember member = teamService.addMemberByEmail(
                    body.get("projectId"),
                    body.get("email")
            );
            return ResponseEntity.ok(member);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Add member manually
    @PostMapping("/members/manual")
    public ResponseEntity<?> addManually(@RequestBody Map<String, String> body) {
        try {
            TeamMember member = teamService.addMemberManually(
                    body.get("projectId"),
                    body.get("name"),
                    body.get("email")
            );
            return ResponseEntity.ok(member);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get team members
    @GetMapping("/members/{projectId}")
    public ResponseEntity<?> getMembers(@PathVariable String projectId) {
        return ResponseEntity.ok(teamService.getTeamMembers(projectId));
    }

    // Assign task
    @PostMapping("/assign")
    public ResponseEntity<?> assignTask(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            String assignedById = jwtService.extractUserId(token);

            TaskAssignment assignment = teamService.assignTask(
                    body.get("taskId"),
                    body.get("assignedToId"),
                    body.get("assignedToName"),
                    body.get("assignedToEmail"),
                    assignedById
            );
            return ResponseEntity.ok(assignment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get task assignments
    @GetMapping("/assignments/task/{taskId}")
    public ResponseEntity<?> getTaskAssignments(@PathVariable String taskId) {
        return ResponseEntity.ok(teamService.getTaskAssignments(taskId));
    }

    // Get developer's assigned tasks
    @GetMapping("/assignments/my")
    public ResponseEntity<?> getMyAssignments(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            String userId = jwtService.extractUserId(token);
            String email = jwtService.extractEmail(token);

            // Get by both userId and email
            List<TaskAssignment> byUserId = taskAssignmentRepository
                    .findByAssignedToId(userId);
            List<TaskAssignment> byEmail = taskAssignmentRepository
                    .findByAssignedToEmail(email);

            // Merge and deduplicate
            Set<String> seen = new HashSet<>();
            List<TaskAssignment> all = new ArrayList<>();
            for (TaskAssignment a : byUserId) {
                if (seen.add(a.getId())) all.add(a);
            }
            for (TaskAssignment a : byEmail) {
                if (seen.add(a.getId())) all.add(a);
            }

            // Return full details
            List<Map<String, Object>> result = all.stream().map(a -> {
                Map<String, Object> map = new HashMap<>();
                map.put("assignmentId", a.getId());
                map.put("status", a.getStatus());
                map.put("assignedAt", a.getAssignedAt());
                map.put("progress", a.getProgress()); // ✅ assignment progress

                // Full task details
                if (a.getTask() != null) {
                    map.put("taskId", a.getTask().getId());
                    map.put("taskTitle", a.getTask().getTitle());
                    map.put("taskStatus", a.getTask().getStatus());
                    map.put("taskProgress", a.getTask().getProgress());
                    map.put("startDate", a.getTask().getStartDate());
                    map.put("endDate", a.getTask().getEndDate());
                    map.put("description", a.getTask().getDescription());
                    map.put("type", a.getTask().getType());
                }

                // Assigned by
                if (a.getAssignedBy() != null) {
                    map.put("assignedByName", a.getAssignedBy().getName());
                }

                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Get all project tasks with assignment info
    @GetMapping("/project/{projectId}/tasks")
    public ResponseEntity<?> getProjectTasks(@PathVariable String projectId) {
        return ResponseEntity.ok(teamService.getProjectTasksWithAssignments(projectId));
    }

    @DeleteMapping("/assign/{assignmentId}")
    public ResponseEntity<?> unassignTask(@PathVariable String assignmentId) {
        try {
            teamService.unassignTask(assignmentId);
            return ResponseEntity.ok(Map.of("message", "Unassigned successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-projects")
    public ResponseEntity<?> getMyProjects(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            String userId = jwtService.extractUserId(token);
            String email = jwtService.extractEmail(token);

            List<Map<String, Object>> projects = teamService.getMyProjects(userId, email);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/assignments/{assignmentId}")
    public ResponseEntity<?> updateAssignment(
            @PathVariable String assignmentId,
            @RequestBody Map<String, Object> body) {
        try {
            TaskAssignment assignment = taskAssignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Assignment not found"));

            // Update status
            if (body.get("status") != null) {
                assignment.setStatus(TaskAssignment.AssignmentStatus
                        .valueOf(body.get("status").toString()));
            }

            // Update progress
            if (body.get("progress") != null) {
                int progress = ((Number) body.get("progress")).intValue();
                assignment.setProgress(progress);

                // Auto update status based on progress
                if (progress == 100) {
                    assignment.setStatus(TaskAssignment.AssignmentStatus.DONE);
                } else if (progress > 0) {
                    assignment.setStatus(TaskAssignment.AssignmentStatus.IN_PROGRESS);
                } else {
                    assignment.setStatus(TaskAssignment.AssignmentStatus.TODO);
                }
            }

            taskAssignmentRepository.save(assignment);

            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "assignmentStatus", assignment.getStatus(),
                    "progress", assignment.getProgress()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
