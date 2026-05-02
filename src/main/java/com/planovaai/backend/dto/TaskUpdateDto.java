package com.planovaai.backend.dto;

public class TaskUpdateDto {
    private String id;  // gantt task UUID
    private Long dbId;      // database ID (optional)
    private int progress;
    private String status;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
