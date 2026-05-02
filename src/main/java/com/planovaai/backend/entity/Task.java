package com.planovaai.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(length = 500)
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private int duration;
    private int progress;
    private String status;
    private String type;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(length = 500)
    private String dependsOn;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

}
