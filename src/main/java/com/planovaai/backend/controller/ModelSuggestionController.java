package com.planovaai.backend.controller;

import com.planovaai.backend.dto.ExtractedTaskDto;
import com.planovaai.backend.dto.GanttTaskDto;
import com.planovaai.backend.entity.Project;
import com.planovaai.backend.repository.ProjectRepository;
import com.planovaai.backend.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ModelSuggestionController(
            ModelSuggestionService modelSuggestionService,
            AiService aiService,
            GanttTaskService ganttTaskService,
            ProjectRepository projectRepository,
            PlanningService planningService,
            TaskExtractionService taskExtractionService
    ) {
        this.modelSuggestionService = modelSuggestionService;
        this.aiService = aiService;
        this.ganttTaskService = ganttTaskService;
        this.projectRepository = projectRepository;
        this.planningService = planningService;
        this.taskExtractionService = taskExtractionService;

    }

    @PostMapping("/full-analysis")
    public ResponseEntity<?> fullAnalysis(@RequestParam MultipartFile file) throws Exception {

        String text = modelSuggestionService.extractTextFromDocument(file);

        Map<String, Object> aiResult = aiService.predictModel(text);
        String model = (String) aiResult.get("model");
        String modelLabel = (String) aiResult.get("modelLabel");

        String complexity = planningService.detectComplexity(text);

        Project project = new Project();
        project.setName("Auto Project");
        projectRepository.save(project);

        planningService.generatePlan(model, complexity, project);

        // Use AI generated tasks for BOTH table and gantt
        List<GanttTaskDto> ganttTask = ganttTaskService.generateFromSrs(text, model, complexity);

        // Convert ganttTask to match what frontend expects for the table
        List<Map<String, Object>> tasks = ganttTask.stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("title", t.getTitle());
            map.put("startDate", t.getStartDate());
            map.put("endDate", t.getEndDate());
            map.put("duration", t.getDuration());
            map.put("status", t.getStatus());
            map.put("progress", t.getProgress());
            return map;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "model", modelLabel,
                "tasks", tasks,        // now shows proper dev tasks in table
                "ganttTask", ganttTask // same data in gantt chart
        ));
    }
}
