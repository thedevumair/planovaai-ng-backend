package com.planovaai.backend.repository;

import com.planovaai.backend.entity.TaskAssignment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, String> {
    List<TaskAssignment> findByTaskId(String taskId);
    List<TaskAssignment> findByAssignedToId(String userId);
    List<TaskAssignment> findByAssignedToEmail(String email);

    @Transactional
    void deleteById(String id);

    List<TaskAssignment> findByAssignedToEmailAndTaskProjectId(
            String email, String projectId
    );

    @Query("SELECT ta FROM TaskAssignment ta " +
            "JOIN ta.task t " +
            "JOIN t.project p " +
            "WHERE p.id = :projectId")
    List<TaskAssignment> findByTaskProjectId(
            @Param("projectId") String projectId
    );
}
