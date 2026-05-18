package com.planovaai.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class TaskAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    @JsonIgnore  // break circular
    private Task task;

    @ManyToOne
    @JoinColumn(name = "assigned_to_id")
    @JsonIgnore  // break circular
    private User assignedTo;

    private String assignedToName;
    private String assignedToEmail;

    @ManyToOne
    @JoinColumn(name = "assigned_by_id")
    @JsonIgnore  // break circular
    private User assignedBy;

    private LocalDateTime assignedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status = AssignmentStatus.TODO; // default TODO

    private int progress = 0; // add progress

    public enum AssignmentStatus {
        TODO, IN_PROGRESS, DONE  // changed ASSIGNED to TODO
    }
}
