package com.planovaai.backend.controller;

import com.planovaai.backend.dto.ExtractedTaskDto;
import com.planovaai.backend.dto.GanttTaskDto;
import com.planovaai.backend.entity.Project;
import com.planovaai.backend.entity.User;
import com.planovaai.backend.repository.ProjectRepository;
import com.planovaai.backend.repository.UserRepository;
import com.planovaai.backend.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/model")
@CrossOrigin(origins = "*")
public class ModelSuggestionController {

    private final ModelSuggestionService modelSuggestionService;
    private final AiService aiService;
    private final TaskExtractionService taskExtractionService;

    private final PlanningService planningService;
    private final GanttTaskService ganttTaskService;
    private final ProjectRepository projectRepository;
    private final  JwtService jwtService;
    private final UserRepository userRepository;
    public ModelSuggestionController(
            ModelSuggestionService modelSuggestionService,
            AiService aiService,
            GanttTaskService ganttTaskService,
            ProjectRepository projectRepository,
            PlanningService planningService,
            TaskExtractionService taskExtractionService,
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.modelSuggestionService = modelSuggestionService;
        this.aiService = aiService;
        this.ganttTaskService = ganttTaskService;
        this.projectRepository = projectRepository;
        this.planningService = planningService;
        this.taskExtractionService = taskExtractionService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;

    }

    @PostMapping("/full-analysis")
    public ResponseEntity<?> fullAnalysis(
            @RequestParam MultipartFile file,
            HttpServletRequest request) {
        try {
            String header = request.getHeader("Authorization");
            String userId = null;
            if (header != null && header.startsWith("Bearer ")) {
                userId = jwtService.extractUserId(header.substring(7));
            }

            String text = modelSuggestionService.extractTextFromDocument(file);

            Map<String, Object> aiResult = aiService.predictModel(text);
            String model = (String) aiResult.get("model");
            String modelLabel = (String) aiResult.get("modelLabel");

            String complexity = planningService.detectComplexity(text);

            Project project = new Project();
            project.setName(file.getOriginalFilename()
                    .replace(".docx", "").replace(".txt", ""));
            project.setDescription("Model: " + model + " | Complexity: " + complexity);

            User currentUser = null;
            if (userId != null) {
                currentUser = userRepository.findById(userId).orElse(null);

                if (currentUser != null) {
                    project.setUser(currentUser);

                    // Promote to TEAM_LEAD when they upload SRS
                    if (currentUser.getRole() == User.Role.DEVELOPER) {
                        currentUser.setRole(User.Role.TEAM_LEAD);
                        userRepository.save(currentUser);
                    }
                }
            }

            projectRepository.save(project);
            planningService.generatePlan(model, complexity, project);

            List<GanttTaskDto> ganttTask = ganttTaskService.generateFromSrs(
                    text, model, complexity, project
            );

            List<Map<String, Object>> tasks = ganttTask.stream().map(t -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", t.getId());
                map.put("title", t.getTitle());
                map.put("description", t.getDescription());
                map.put("startDate", t.getStartDate());
                map.put("endDate", t.getEndDate());
                map.put("duration", t.getDuration());
                map.put("status", t.getStatus());
                map.put("progress", t.getProgress());
                map.put("type", t.getType());
                map.put("dependsOn", t.getDependsOn());
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "model", modelLabel,
                    "projectId", project.getId(),
                    "tasks", tasks,
                    "ganttTask", ganttTask,
                    "role", currentUser != null ? currentUser.getRole().name() : "DEVELOPER"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
