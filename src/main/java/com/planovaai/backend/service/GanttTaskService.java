package com.planovaai.backend.service;

import com.planovaai.backend.dto.ExtractedTaskDto;
import com.planovaai.backend.dto.GanttTaskDto;
import com.planovaai.backend.entity.Project;
import com.planovaai.backend.entity.Task;
import com.planovaai.backend.repository.TaskRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

@Service
public class GanttTaskService {

    private final TaskRepository taskRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public GanttTaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // Backward compatible — no project
    public List<GanttTaskDto> generateFromSrs(String srsText, String model, String complexity) {
        return generateFromSrs(srsText, model, complexity, null);
    }

    public List<GanttTaskDto> generateFromSrs(String srsText, String model, String complexity, Project project) {

        List<GanttTaskDto> list = new ArrayList<>();

        try {
            String url = "http://localhost:5000/generate-tasks";

            Map<String, String> request = new HashMap<>();
            request.put("text", srsText);
            request.put("model", model);
            request.put("complexity", complexity);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) response.getBody().get("tasks");

            LocalDate currentDate = LocalDate.now();

            // ✅ Delete old tasks for this project before saving new ones
            if (project != null) {
                taskRepository.deleteByProjectId(project.getId());
            }

            for (int i = 0; i < tasks.size(); i++) {
                Map<String, Object> task = tasks.get(i);

                int duration = task.get("estimatedDays") != null
                        ? ((Number) task.get("estimatedDays")).intValue()
                        : 5;

                LocalDate start = currentDate;
                LocalDate end = start.plusDays(duration);

                GanttTaskDto dto = new GanttTaskDto();
                dto.setTitle((String) task.get("title"));
                dto.setDescription((String) task.getOrDefault("description", ""));
                dto.setStartDate(start);
                dto.setEndDate(end);
                dto.setDuration(duration);
                dto.setProgress(0);
                dto.setStatus("Planned");
                dto.setType((String) task.getOrDefault("type", "FR"));

                if (i > 0) {
                    dto.setDependsOn(list.get(i - 1).getId());
                }

                if (project != null) {
                    // ✅ Save to DB — DB generates UUID automatically
                    Task entity = new Task();
                    entity.setTitle(dto.getTitle());
                    entity.setDescription(dto.getDescription());
                    entity.setStartDate(dto.getStartDate());
                    entity.setEndDate(dto.getEndDate());
                    entity.setDuration(dto.getDuration());
                    entity.setProgress(0);
                    entity.setStatus("Planned");
                    entity.setType(dto.getType());
                    entity.setDependsOn(dto.getDependsOn());
                    entity.setProject(project);
                    taskRepository.save(entity);

                    // ✅ Use DB generated UUID — no toString() needed since id is String
                    dto.setId(entity.getId());

                } else {
                    // ✅ No project — generate UUID manually for session only
                    dto.setId(UUID.randomUUID().toString());
                }

                currentDate = end;
                list.add(dto);
            }

            System.out.println("✅ Saved " + list.size() + " tasks to DB");

        } catch (Exception e) {
            System.err.println("❌ Flask task generation failed: " + e.getMessage());
        }

        return list;
    }
}
