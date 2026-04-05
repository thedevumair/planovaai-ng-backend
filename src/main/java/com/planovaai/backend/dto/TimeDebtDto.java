package com.planovaai.backend.dto;

import lombok.Data;

@Data
public class TimeDebtDto {
    private String title;
    private int planned;
    private int actual;
    private int delay;
}
