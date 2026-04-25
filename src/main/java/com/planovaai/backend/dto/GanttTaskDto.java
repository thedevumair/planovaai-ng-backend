package com.planovaai.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class GanttTaskDto {

    private String id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private int duration;
    private int progress;
    private String dependsOn;
    private String status;
    private String type;
}
