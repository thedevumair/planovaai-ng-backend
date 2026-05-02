package com.planovaai.backend.repository;

import com.planovaai.backend.entity.TaskAssignment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, String> {
    List<TaskAssignment> findByTaskId(String taskId);
    List<TaskAssignment> findByAssignedToId(String userId);
    boolean existsByTaskIdAndAssignedToId(String taskId, String userId);

    // Add this
    @Transactional
    void deleteByTaskIdAndAssignedToEmail(String taskId, String assignedToEmail);
}
