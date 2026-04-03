package com.planovaai.backend.dto;

import lombok.Data;

@Data
public class GanttTaskDto {

    public String title;
    public int startDay;
    public int endDay;
}
