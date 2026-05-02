package com.planovaai.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TimeDebtTaskDto {
    private String id;
    private String title;
    private LocalDate plannedStart;
    private LocalDate plannedEnd;
    private LocalDate recoveryEnd;
    private int progress;
    private String status;
    private int duration;
    private int debtDays;
    private int overdueDays;
    private int progressGap;
    private boolean delayed;
    private boolean behindSchedule;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDate getPlannedStart() { return plannedStart; }
    public void setPlannedStart(LocalDate plannedStart) { this.plannedStart = plannedStart; }
    public LocalDate getPlannedEnd() { return plannedEnd; }
    public void setPlannedEnd(LocalDate plannedEnd) { this.plannedEnd = plannedEnd; }
    public LocalDate getRecoveryEnd() { return recoveryEnd; }
    public void setRecoveryEnd(LocalDate recoveryEnd) { this.recoveryEnd = recoveryEnd; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public int getDebtDays() { return debtDays; }
    public void setDebtDays(int debtDays) { this.debtDays = debtDays; }
    public int getOverdueDays() { return overdueDays; }
    public void setOverdueDays(int overdueDays) { this.overdueDays = overdueDays; }
    public int getProgressGap() { return progressGap; }
    public void setProgressGap(int progressGap) { this.progressGap = progressGap; }
    public boolean isDelayed() { return delayed; }
    public void setDelayed(boolean delayed) { this.delayed = delayed; }
    public boolean isBehindSchedule() { return behindSchedule; }
    public void setBehindSchedule(boolean behindSchedule) { this.behindSchedule = behindSchedule; }
}
