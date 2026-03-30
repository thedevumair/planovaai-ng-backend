package com.planovaai.backend.repository;

import com.planovaai.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
