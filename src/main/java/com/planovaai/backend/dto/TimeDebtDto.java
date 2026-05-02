package com.planovaai.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;


@Data
public class TimeDebtDto {
    private int totalDebtDays;
    private int delayedTaskCount;
    private String riskLevel;
    private LocalDate recoveryDate;
    private List<TimeDebtTaskDto> tasks;

    public int getTotalDebtDays() { return totalDebtDays; }
    public void setTotalDebtDays(int totalDebtDays) { this.totalDebtDays = totalDebtDays; }
    public int getDelayedTaskCount() { return delayedTaskCount; }
    public void setDelayedTaskCount(int delayedTaskCount) { this.delayedTaskCount = delayedTaskCount; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public LocalDate getRecoveryDate() { return recoveryDate; }
    public void setRecoveryDate(LocalDate recoveryDate) { this.recoveryDate = recoveryDate; }
    public List<TimeDebtTaskDto> getTasks() { return tasks; }
    public void setTasks(List<TimeDebtTaskDto> tasks) { this.tasks = tasks; }
}
