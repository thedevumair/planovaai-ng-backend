package com.planovaai.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // The project this team belongs to
    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonIgnore  // break circular
    private Project project;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore  // break circular
    private User user;

    private String developerName;
    private String developerEmail;

    @Enumerated(EnumType.STRING)
    private MemberStatus status = MemberStatus.ACTIVE;

    public enum MemberStatus {
        INVITED, ACTIVE
    }
}
