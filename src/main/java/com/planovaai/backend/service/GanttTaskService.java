package com.planovaai.backend.service;

import com.planovaai.backend.dto.ExtractedTaskDto;
import com.planovaai.backend.dto.GanttTaskDto;
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

    public List<GanttTaskDto> generateFromSrs(String srsText, String model, String complexity) {

        List<GanttTaskDto> list = new ArrayList<>();

        try {
            // ✅ Call Flask AI endpoint
            String url = "http://localhost:5000/generate-tasks";

            Map<String, String> request = new HashMap<>();
            request.put("text", srsText);
            request.put("model", model);
            request.put("complexity", complexity);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) response.getBody().get("tasks");

            LocalDate currentDate = LocalDate.now();

            for (int i = 0; i < tasks.size(); i++) {
                Map<String, Object> task = tasks.get(i);

                GanttTaskDto dto = new GanttTaskDto();
                dto.setId(UUID.randomUUID().toString());
                dto.setTitle((String) task.get("title"));

                int duration = task.get("estimatedDays") != null
                        ? ((Number) task.get("estimatedDays")).intValue()
                        : 5;

                LocalDate start = currentDate;
                LocalDate end = start.plusDays(duration);

                dto.setStartDate(start);
                dto.setEndDate(end);
                dto.setDuration(duration);
                dto.setProgress(0);
                dto.setStatus("Planned");
                dto.setType((String) task.getOrDefault("type", "FR"));
                dto.setDescription((String) task.getOrDefault("description", ""));

                if (i > 0) {
                    dto.setDependsOn(list.get(i - 1).getId());
                }

                currentDate = end;
                list.add(dto);
            }

        } catch (Exception e) {
            System.err.println("❌ Flask task generation failed: " + e.getMessage());
        }

        return list;
    }
}
