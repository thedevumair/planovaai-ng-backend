package com.planovaai.backend.controller;

import com.planovaai.backend.entity.GitHubActivity;
import com.planovaai.backend.entity.TaskAssignment;
import com.planovaai.backend.repository.GitHubActivityRepository;
import com.planovaai.backend.repository.TaskAssignmentRepository;
import com.planovaai.backend.repository.TaskRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/github/stats")
@CrossOrigin(origins = "*")
public class GitHubStatsController {

    private final GitHubActivityRepository gitHubActivityRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskRepository taskRepository;

    public GitHubStatsController(
            GitHubActivityRepository gitHubActivityRepository,
            TaskAssignmentRepository taskAssignmentRepository,
            TaskRepository taskRepository
    ) {
        this.gitHubActivityRepository = gitHubActivityRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.taskRepository = taskRepository;
    }

    // Get stats for all developers in a project
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getProjectStats(@PathVariable String projectId) {
        try {
            // Use direct query — no lazy loading issue
            List<TaskAssignment> assignments =
                    taskAssignmentRepository.findByTaskProjectId(projectId);

            System.out.println("🔍 Assignments for project "
                    + projectId + ": " + assignments.size());

            if (assignments.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            Map<String, List<TaskAssignment>> byDev = assignments.stream()
                    .collect(Collectors.groupingBy(
                            TaskAssignment::getAssignedToEmail
                    ));

            List<Map<String, Object>> devStats = new ArrayList<>();

            for (Map.Entry<String, List<TaskAssignment>> entry
                    : byDev.entrySet()) {

                String email = entry.getKey();
                List<TaskAssignment> devAssignments = entry.getValue();

                List<GitHubActivity> activities =
                        gitHubActivityRepository
                                .findByDeveloperEmailOrderByPushedAtDesc(email);

                System.out.println("🔍 GitHub activities for "
                        + email + ": " + activities.size());

                LocalDateTime lastPush = activities.isEmpty()
                        ? null : activities.get(0).getPushedAt();

                long daysSinceLastPush = lastPush == null ? -1
                        : ChronoUnit.DAYS.between(lastPush, LocalDateTime.now());

                int totalCommits = activities.size();

                int totalLinesAdded = activities.stream()
                        .mapToInt(GitHubActivity::getLinesAdded).sum();

                double commitFrequency = 0;
                if (!activities.isEmpty() && lastPush != null) {
                    LocalDateTime firstPush = activities
                            .get(activities.size() - 1).getPushedAt();
                    long totalActiveDays = Math.max(1,
                            ChronoUnit.DAYS.between(firstPush, LocalDateTime.now()));
                    commitFrequency = (double) totalCommits / totalActiveDays;
                }

                String riskLevel;
                if (lastPush == null) riskLevel = "NO_ACTIVITY";
                else if (daysSinceLastPush >= 3) riskLevel = "AT_RISK";
                else if (daysSinceLastPush >= 1) riskLevel = "MODERATE";
                else riskLevel = "ACTIVE";

                int avgProgress = devAssignments.stream()
                        .mapToInt(TaskAssignment::getProgress)
                        .sum() / Math.max(1, devAssignments.size());

                List<Map<String, Object>> recentCommits = activities
                        .stream().limit(5).map(a -> {
                            Map<String, Object> cm = new HashMap<>();
                            cm.put("message", a.getCommitMessage());
                            cm.put("pushedAt", a.getPushedAt());
                            cm.put("linesAdded", a.getLinesAdded());
                            cm.put("repo", a.getRepoName());
                            return cm;
                        }).collect(Collectors.toList());

                Map<String, Object> stat = new HashMap<>();
                stat.put("email", email);
                stat.put("name", devAssignments.get(0).getAssignedToName());
                stat.put("assignedTasks", devAssignments.size());
                stat.put("avgProgress", avgProgress);
                stat.put("totalCommits", totalCommits);
                stat.put("totalLinesAdded", totalLinesAdded);
                stat.put("commitFrequency",
                        Math.round(commitFrequency * 10.0) / 10.0);
                stat.put("lastPush",
                        lastPush != null ? lastPush.toString() : null);
                stat.put("daysSinceLastPush", daysSinceLastPush);
                stat.put("riskLevel", riskLevel);
                stat.put("recentCommits", recentCommits);

                devStats.add(stat);
            }

            return ResponseEntity.ok(devStats);

        } catch (Exception e) {
            System.err.println("❌ Stats error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/debug/{projectId}")
    public ResponseEntity<?> debug(@PathVariable String projectId) {
        Map<String, Object> result = new HashMap<>();

        // Check assignments
        List<TaskAssignment> all = taskAssignmentRepository.findAll();
        result.put("totalAssignments", all.size());

        List<TaskAssignment> byProject =
                taskAssignmentRepository.findByTaskProjectId(projectId);
        result.put("assignmentsByProject", byProject.size());

        // Check GitHub activities
        List<GitHubActivity> activities = gitHubActivityRepository.findAll();
        result.put("totalActivities", activities.size());
        result.put("activityEmails", activities.stream()
                .map(GitHubActivity::getDeveloperEmail)
                .distinct()
                .collect(Collectors.toList()));

        return ResponseEntity.ok(result);
    }
}
