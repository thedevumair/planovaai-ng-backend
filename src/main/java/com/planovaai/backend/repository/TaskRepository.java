package com.planovaai.backend.repository;

import com.planovaai.backend.entity.Task;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository  extends JpaRepository<Task, String> {
    List<Task> findByProjectIdOrderByStartDateAsc(String projectId);
    List<Task> findByProjectId(String projectId);

    @Transactional
    void deleteByProjectId(String projectId);
}
