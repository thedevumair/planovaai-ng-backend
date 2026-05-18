package com.planovaai.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class GitHubActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String developerEmail;
    private String taskId;
    private String commitMessage;
    private String commitHash;
    private int linesAdded;
    private int linesDeleted;
    private LocalDateTime pushedAt;
    private String repoName;
    private String branch;

    @ManyToOne
    @JoinColumn(name = "assignment_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private TaskAssignment assignment;
}
