package com.planovaai.backend.controller;

import com.planovaai.backend.service.GanttTaskService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/gantt")
@CrossOrigin(origins = "*")
public class GanttTaskController {

    public final GanttTaskService ganttTaskService;

    public GanttTaskController(GanttTaskService ganttTaskService) {
        this.ganttTaskService = ganttTaskService;
    }
}
