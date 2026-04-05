package com.planovaai.backend.dto;

import lombok.Data;

@Data
public class GanttTaskDto {

    private String title;
    private int startDay;
    private int endDay;
}
