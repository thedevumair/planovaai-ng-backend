package com.planovaai.backend.repository;

import com.planovaai.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, String> {
    List<Project> findByUserIdOrderByIdDesc(String userId);
}
