package com.planovaai.backend.service;

import com.planovaai.backend.dto.GanttTaskDto;
import com.planovaai.backend.entity.Task;
import com.planovaai.backend.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GanttTaskService {

    private final TaskRepository taskRepository;

    public GanttTaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<GanttTaskDto> generateGantt(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);

        List<GanttTaskDto> ganttTaskDtoList = new ArrayList<>();

        int currentDay = 1;

        for (Task task : tasks) {
            GanttTaskDto dto = new GanttTaskDto();

            dto.setTitle(task.getTitle());
            dto.setStartDay(currentDay);
            dto.setEndDay(currentDay + task.getDuration());

            currentDay += task.getDuration();

            ganttTaskDtoList.add(dto);
        }

        return ganttTaskDtoList;
    }
}
