package com.planovaai.backend.repository;

import com.planovaai.backend.entity.GitHubActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GitHubActivityRepository
        extends JpaRepository<GitHubActivity, String> {

    List<GitHubActivity> findByDeveloperEmailOrderByPushedAtDesc(
            String email
    );

    List<GitHubActivity> findByTaskIdOrderByPushedAtDesc(String taskId);

    List<GitHubActivity> findByDeveloperEmailAndTaskId(
            String email, String taskId
    );

    // For delay detection
    List<GitHubActivity> findByDeveloperEmailAndPushedAtAfter(
            String email, LocalDateTime since
    );

    long countByDeveloperEmailAndTaskId(String email, String taskId);
}
