package com.planovaai.backend.controller;

import com.planovaai.backend.entity.TaskAssignment;
import com.planovaai.backend.entity.TeamMember;
import com.planovaai.backend.service.JwtService;
import com.planovaai.backend.service.TeamService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/team")
@CrossOrigin(origins = "*")
public class TeamController {

    private final TeamService teamService;
    private final JwtService jwtService;

    public TeamController(TeamService teamService, JwtService jwtService) {
        this.teamService = teamService;
        this.jwtService = jwtService;
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
        String token = request.getHeader("Authorization").substring(7);
        String userId = jwtService.extractUserId(token);
        return ResponseEntity.ok(teamService.getDeveloperAssignments(userId));
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
}
