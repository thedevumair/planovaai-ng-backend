package com.planovaai.backend.service;

import com.planovaai.backend.entity.Project;
import com.planovaai.backend.entity.Task;
import com.planovaai.backend.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlanningService {

    private final TaskRepository taskRepository;

    public PlanningService(
            TaskRepository taskRepository
    ) {
        this.taskRepository = taskRepository;
    }

    // STEP 1 Detect complexity
    public String detectComplexity(String text) {

        int length = text.length();

        if (length < 2000) return "SMALL";
        if (length < 6000) return "MEDIUM";
        return "LARGE";
    }
    // STEP 2 Generate tasks dynamically
    public List<Task> generatePlan(String model, String complexity, Project project) {

        List<Task> tasks = new ArrayList<>();

        int multiplier = switch (complexity) {
            case "SMALL" -> 1;
            case "MEDIUM" -> 2;
            case "LARGE" -> 3;
            default -> 1;
        };

        if (model.equalsIgnoreCase("Agile")) {
            tasks.add(createTask("Sprint Planning", 2 * multiplier, project));
            tasks.add(createTask("Development", 7 * multiplier, project));
            tasks.add(createTask("Testing", 3 * multiplier, project));
        }

        else if (model.equalsIgnoreCase("Waterfall")) {
            tasks.add(createTask("Requirement Analysis", 4 * multiplier, project));
            tasks.add(createTask("Design", 5 * multiplier, project));
            tasks.add(createTask("Implementation", 10 * multiplier, project));
            tasks.add(createTask("Testing", 6 * multiplier, project));
        }

        else if (model.equalsIgnoreCase("Spiral")) {
            tasks.add(createTask("Planning", 3 * multiplier, project));
            tasks.add(createTask("Risk Analysis", 5 * multiplier, project));
            tasks.add(createTask("Development", 8 * multiplier, project));
        }

        assignDates(tasks);

        return taskRepository.saveAll(tasks);
    }

    private Task createTask(String title, int duration, Project project) {

        Task task = new Task();
        task.setTitle(title);
        task.setDuration(duration);
        task.setStatus("Planned");
        task.setProject(project);
//        task.setResponsible("Team Member");

        return task;
    }

    // STEP 3 Assign dates automatically
    private void assignDates(List<Task> tasks) {

        LocalDate current = LocalDate.now();

        for (Task task : tasks) {

            task.setStartDate(current);

            LocalDate end = current.plusDays(task.getDuration());
            task.setEndDate(end);

            current = end;
        }
    }
}
