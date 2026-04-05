package com.planovaai.backend.controller;

import com.planovaai.backend.dto.GanttTaskDto;
import com.planovaai.backend.service.GanttTaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gantt")
public class GanttTaskController {

    public final GanttTaskService ganttTaskService;

    public GanttTaskController(GanttTaskService ganttTaskService) {
        this.ganttTaskService = ganttTaskService;
    }

    @GetMapping("/project/{projectId}")
    public List<GanttTaskDto> generateGantt(@PathVariable Long projectId) {
        return ganttTaskService.generateGantt(projectId);
    }

    @GetMapping("/smart/project/{projectId}")
    public List<GanttTaskDto> generateSmartGantt(@PathVariable Long projectId) {
        return ganttTaskService.generateSmartGantt(projectId);
    }
}
