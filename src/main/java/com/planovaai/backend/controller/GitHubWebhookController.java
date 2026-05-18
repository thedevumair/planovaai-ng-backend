package com.planovaai.backend.controller;

import com.planovaai.backend.entity.GitHubActivity;
import com.planovaai.backend.entity.Task;
import com.planovaai.backend.entity.TaskAssignment;
import com.planovaai.backend.repository.GitHubActivityRepository;
import com.planovaai.backend.repository.TaskAssignmentRepository;
import com.planovaai.backend.repository.TaskRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/github")
@CrossOrigin(origins = "*")
public class GitHubWebhookController {

    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskRepository taskRepository;
    private final GitHubActivityRepository gitHubActivityRepository;

    public GitHubWebhookController(
            TaskAssignmentRepository taskAssignmentRepository,
            TaskRepository taskRepository,
            GitHubActivityRepository gitHubActivityRepository
    ) {
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.taskRepository = taskRepository;
        this.gitHubActivityRepository = gitHubActivityRepository;
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-GitHub-Event",
                    defaultValue = "") String event) {

        System.out.println("📦 GitHub Event: " + event);

        if (!"push".equals(event)) {
            return ResponseEntity.ok(
                    Map.of("message", "Event ignored: " + event)
            );
        }

        try {
            List<Map<String, Object>> commits =
                    (List<Map<String, Object>>) payload.get("commits");

            Map<String, Object> repoObj =
                    (Map<String, Object>) payload.get("repository");
            String repoName = repoObj != null
                    ? (String) repoObj.get("full_name") : "unknown";

            String branch = "";
            String ref = (String) payload.get("ref");
            if (ref != null) branch = ref.replace("refs/heads/", "");

            if (commits == null || commits.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "No commits"));
            }

            List<String> processed = new ArrayList<>();

            for (Map<String, Object> commit : commits) {
                String message = (String) commit.get("message");
                String commitHash = (String) commit.get("id");

                // Get author email
                String authorEmail = "";
                Map<String, Object> authorObj =
                        (Map<String, Object>) commit.get("author");
                if (authorObj != null) {
                    authorEmail = (String) authorObj
                            .getOrDefault("email", "");
                }

                // Get lines added/deleted
                int linesAdded = 0;
                int linesDeleted = 0;
                List<Map<String, Object>> added =
                        (List<Map<String, Object>>) commit.get("added");
                List<Map<String, Object>> modified =
                        (List<Map<String, Object>>) commit.get("modified");

                // GitHub push event doesn't include line counts directly
                // We estimate based on files changed
                int filesChanged = 0;
                if (added != null) filesChanged += added.size();
                if (modified != null) filesChanged += modified.size();
                linesAdded = filesChanged * 20; // estimate

                System.out.println("📝 Commit: " + message
                        + " by " + authorEmail);

                String result = processCommit(
                        message, authorEmail, commitHash,
                        repoName, branch, linesAdded, linesDeleted
                );
                processed.add(result);
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Webhook processed",
                    "results", processed
            ));

        } catch (Exception e) {
            System.err.println("❌ Webhook error: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private String processCommit(
            String commitMessage, String authorEmail,
            String commitHash, String repoName,
            String branch, int linesAdded, int linesDeleted) {

        if (commitMessage == null) return "No message";

        String lower = commitMessage.toLowerCase();
        int progress = detectProgress(lower);
        String status = progress == 100 ? "DONE"
                : progress > 0    ? "IN_PROGRESS"
                : "TODO";

        String frNumber = extractFrNumber(commitMessage);
        Task matchedTask = null;

        System.out.println("🔍 FR Number extracted: " + frNumber);
        System.out.println("🔍 Author email: " + authorEmail);
        System.out.println("🔍 Progress detected: " + progress);

        // REPLACE OLD if (frNumber != null) block WITH THIS:
        if (frNumber != null) {
            String normalizedFr = frNumber
                    .replaceAll("(?i)(FR|NFR)[\\s\\-]?", "")
                    .trim();

            boolean isNfr = frNumber.toUpperCase().startsWith("NFR");
            String prefix = isNfr ? "nfr" : "fr";

            System.out.println("🔍 Normalized FR: " + normalizedFr
                    + " prefix: " + prefix);

            String finalNorm = normalizedFr;
            String finalPrefix = prefix;

            matchedTask = taskRepository.findAll().stream()
                    .filter(t -> t.getTitle() != null)
                    .filter(t -> {
                        String title = t.getTitle().toLowerCase().trim();
                        Pattern taskFrPattern = Pattern.compile(
                                "^(?:fr|nfr)[\\s\\-]?(\\d+\\.\\d+(?:\\.\\d+)?)\\b",
                                Pattern.CASE_INSENSITIVE
                        );
                        Matcher taskMatcher = taskFrPattern.matcher(title);
                        if (taskMatcher.find()) {
                            String taskFrNum = taskMatcher.group(1);
                            return taskFrNum.equals(finalNorm);
                        }
                        return false;
                    })
                    .findFirst()
                    .orElse(null);

            System.out.println("🔍 Exact match result: " +
                    (matchedTask != null ? matchedTask.getTitle() : "none"));

            if (matchedTask == null) {
                matchedTask = taskRepository.findAll().stream()
                        .filter(t -> t.getTitle() != null)
                        .filter(t -> t.getTitle().toLowerCase()
                                .contains(finalPrefix + " " + finalNorm))
                        .min(Comparator.comparingInt(t -> t.getTitle().length()))
                        .orElse(null);

                System.out.println("🔍 Fallback match result: " +
                        (matchedTask != null ? matchedTask.getTitle() : "none"));
            }
        }

        // Keyword fallback if frNumber was null or no match
        if (matchedTask == null) {
            matchedTask = taskRepository.findAll().stream()
                    .filter(t -> t.getTitle() != null &&
                            hasKeywordMatch(lower, t.getTitle().toLowerCase()))
                    .findFirst().orElse(null);
            System.out.println("🔍 Keyword match result: " +
                    (matchedTask != null ? matchedTask.getTitle() : "none"));
        }

        System.out.println("🔍 Final matched task: " +
                (matchedTask != null ? matchedTask.getTitle() : "NONE"));

        if (matchedTask != null) {
            GitHubActivity activity = new GitHubActivity();
            activity.setDeveloperEmail(authorEmail);
            activity.setTaskId(matchedTask.getId());
            activity.setCommitMessage(commitMessage);
            activity.setCommitHash(commitHash);
            activity.setLinesAdded(linesAdded);
            activity.setLinesDeleted(linesDeleted);
            activity.setPushedAt(LocalDateTime.now());
            activity.setRepoName(repoName);
            activity.setBranch(branch);

            List<TaskAssignment> assignments =
                    taskAssignmentRepository.findByTaskId(matchedTask.getId());

            System.out.println("🔍 Assignments found: " + assignments.size());

            boolean updated = false;

            for (TaskAssignment assignment : assignments) {
                System.out.println("🔍 Checking: "
                        + assignment.getAssignedToEmail()
                        + " vs " + authorEmail);

                if (authorEmail.isEmpty() ||
                        authorEmail.equalsIgnoreCase(
                                assignment.getAssignedToEmail())) {

                    activity.setAssignment(assignment);

                    if (progress > assignment.getProgress()) {
                        assignment.setProgress(progress);
                        assignment.setStatus(
                                TaskAssignment.AssignmentStatus.valueOf(status)
                        );
                        taskAssignmentRepository.save(assignment);
                        updated = true;
                        System.out.println(" Updated: "
                                + assignment.getAssignedToEmail()
                                + " → " + progress + "%");
                    } else {
                        System.out.println("⚠️ Not updated: "
                                + progress + "% <= existing "
                                + assignment.getProgress() + "%");
                    }
                    break;
                }
            }

            if (!updated && !assignments.isEmpty()) {
                System.out.println("❌ Email mismatch!");
                System.out.println("   Commit: " + authorEmail);
                assignments.forEach(a ->
                        System.out.println("   DB: " + a.getAssignedToEmail()));
            }

            gitHubActivityRepository.save(activity);
            return " Task: " + matchedTask.getTitle()
                    + " → " + progress + "%";
        }

        // Save unmatched activity
        GitHubActivity activity = new GitHubActivity();
        activity.setDeveloperEmail(authorEmail);
        activity.setCommitMessage(commitMessage);
        activity.setCommitHash(commitHash);
        activity.setLinesAdded(linesAdded);
        activity.setPushedAt(LocalDateTime.now());
        activity.setRepoName(repoName);
        activity.setBranch(branch);
        gitHubActivityRepository.save(activity);

        return "⚠️ No task matched: " + commitMessage;
    }

    private String extractFrNumber(String message) {
        // Match FR-3.1, FR 3.1, NFR-5.1 etc
        Pattern pattern = Pattern.compile(
                "\\b((?:FR|NFR)[\\s\\-]?\\d+\\.\\d+(?:\\.\\d+)?)\\b",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(message);

        String longestMatch = null;
        while (matcher.find()) {
            String match = matcher.group(1).trim();
            // Keep longest match (FR-3.1.1 > FR-3.1)
            if (longestMatch == null ||
                    match.length() > longestMatch.length()) {
                longestMatch = match;
            }
        }
        return longestMatch;
    }

    private int detectProgress(String message) {
        // Check explicit percentage FIRST — highest priority
        Pattern pctPattern = Pattern.compile("(\\d+)\\s*%");
        Matcher pctMatcher = pctPattern.matcher(message);
        if (pctMatcher.find()) {
            return Math.min(100, Math.max(0,
                    Integer.parseInt(pctMatcher.group(1))));
        }

        // Keywords only if no percentage found
        if (message.contains("done") || message.contains("complete")
                || message.contains("finished") || message.contains("closes"))
            return 100;
        if (message.contains("feat:") || message.contains("fix:"))
            return 100;
        if (message.contains("wip") || message.contains("in progress")
                || message.contains("working") || message.contains("update")
                || message.contains("refactor") || message.contains("chore:"))
            return 50;
        if (message.contains("start") || message.contains("init")
                || message.contains("begin") || message.contains("setup"))
            return 25;
        return 30;
    }

    private boolean hasKeywordMatch(String commitMsg, String taskTitle) {
        String[] taskWords = taskTitle
                .replaceAll("[^a-z\\s]", "").split("\\s+");
        int matches = 0;
        for (String word : taskWords) {
            if (word.length() > 3 && commitMsg.contains(word)) matches++;
        }
        return matches >= 2;
    }
}
