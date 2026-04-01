package com.planovaai.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String title;
    public String status;
    public int duration;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
}
