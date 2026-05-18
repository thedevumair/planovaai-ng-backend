package com.planovaai.backend.service;

import com.planovaai.backend.entity.*;
import com.planovaai.backend.repository.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private final TeamMemberRepository teamMemberRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TeamService(
            TeamMemberRepository teamMemberRepository,
            TaskAssignmentRepository taskAssignmentRepository,
            UserRepository userRepository,
            TaskRepository taskRepository,
            ProjectRepository projectRepository
    ) {
        this.teamMemberRepository = teamMemberRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    // Add developer by email (has account)
    public TeamMember addMemberByEmail(String projectId, String email) {
        Optional<User> user = userRepository.findByEmail(email);

        TeamMember member = new TeamMember();
        member.setProject(projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found")));
        member.setDeveloperEmail(email);

        if (user.isPresent()) {
            member.setUser(user.get());
            member.setDeveloperName(user.get().getName()); // set name from user
            member.setStatus(TeamMember.MemberStatus.ACTIVE);
        } else {
            // Extract name from email (before @)
            String nameFromEmail = email.substring(0, email.indexOf('@'));
            nameFromEmail = nameFromEmail.substring(0, 1).toUpperCase()
                    + nameFromEmail.substring(1); // capitalize
            member.setDeveloperName(nameFromEmail);
            member.setStatus(TeamMember.MemberStatus.INVITED);
        }

        return teamMemberRepository.save(member);
    }

    // Add developer manually (no account)
    public TeamMember addMemberManually(String projectId, String name, String email) {
        TeamMember member = new TeamMember();
        member.setProject(projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found")));
        member.setDeveloperName(name);
        member.setDeveloperEmail(email);
        member.setStatus(TeamMember.MemberStatus.ACTIVE);
        return teamMemberRepository.save(member);
    }

    // Get all team members for project
    public List<Map<String, Object>> getTeamMembers(String projectId) {
        List<TeamMember> members = teamMemberRepository.findByProjectId(projectId);

        return members.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("developerName", m.getDeveloperName());
            map.put("developerEmail", m.getDeveloperEmail());
            map.put("status", m.getStatus());
            // include user info safely
            if (m.getUser() != null) {
                map.put("userId", m.getUser().getId());
                map.put("userName", m.getUser().getName());
            }
            return map;
        }).collect(Collectors.toList());
    }

    // Assign task to developer
    public TaskAssignment assignTask(String taskId, String assignedToId,
                                     String assignedToName, String assignedToEmail,
                                     String assignedById) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User assignedBy = userRepository.findById(assignedById)
                .orElseThrow(() -> new RuntimeException("Team lead not found"));

        TaskAssignment assignment = new TaskAssignment();
        assignment.setTask(task);
        assignment.setAssignedBy(assignedBy);
        assignment.setAssignedToName(assignedToName);
        assignment.setAssignedToEmail(assignedToEmail);

        if (assignedToId != null && !assignedToId.isBlank()) {
            userRepository.findById(assignedToId).ifPresent(assignment::setAssignedTo);
        }

        return taskAssignmentRepository.save(assignment);
    }

    // Get all assignments for a task
    public List<TaskAssignment> getTaskAssignments(String taskId) {
        return taskAssignmentRepository.findByTaskId(taskId);
    }

    // Get all tasks assigned to a developer
    public List<TaskAssignment> getDeveloperAssignments(String userId) {
        return taskAssignmentRepository.findByAssignedToId(userId);
    }

    // Get project tasks with assignment info
    public List<Map<String, Object>> getProjectTasksWithAssignments(String projectId) {
        List<Task> tasks = taskRepository
                .findByProjectIdOrderByStartDateAsc(projectId);

        return tasks.stream().map(task -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", task.getId());
            map.put("title", task.getTitle());
            map.put("status", task.getStatus());
            map.put("progress", task.getProgress());
            map.put("startDate", task.getStartDate());
            map.put("endDate", task.getEndDate());
            map.put("duration", task.getDuration());
            map.put("type", task.getType());

            List<TaskAssignment> assignments =
                    taskAssignmentRepository.findByTaskId(task.getId());

            // ✅ Calculate average developer progress
            int avgProgress = 0;
            if (!assignments.isEmpty()) {
                avgProgress = assignments.stream()
                        .mapToInt(TaskAssignment::getProgress)
                        .sum() / assignments.size();
            }
            map.put("developerProgress", avgProgress);

            List<Map<String, Object>> assignees = assignments.stream().map(a -> {
                Map<String, Object> aMap = new HashMap<>();
                aMap.put("assignmentId", a.getId());
                aMap.put("name", a.getAssignedToName());
                aMap.put("email", a.getAssignedToEmail());
                aMap.put("status", a.getStatus());
                aMap.put("progress", a.getProgress()); // ✅ developer progress
                return aMap;
            }).collect(Collectors.toList());

            map.put("assignees", assignees);
            map.put("isAssigned", !assignments.isEmpty());
            return map;
        }).collect(Collectors.toList());
    }

    public void unassignTask(String assignmentId) {
        taskAssignmentRepository.deleteById(assignmentId);
    }

    public List<Map<String, Object>> getMyProjects(String userId, String email) {

        // ✅ Find all team memberships by userId or email
        List<TeamMember> memberships = teamMemberRepository
                .findByUserIdOrDeveloperEmail(userId, email);

        return memberships.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            Project project = m.getProject();
            map.put("projectId", project.getId());
            map.put("projectName", project.getName());
            map.put("description", project.getDescription());
            map.put("role", "DEVELOPER");
            map.put("memberStatus", m.getStatus());

            // ✅ Count assigned tasks
            List<TaskAssignment> assignments = taskAssignmentRepository
                    .findByAssignedToEmailAndTaskProjectId(email, project.getId());
            map.put("assignedTasks", assignments.size());

            return map;
        }).collect(Collectors.toList());
    }
}
