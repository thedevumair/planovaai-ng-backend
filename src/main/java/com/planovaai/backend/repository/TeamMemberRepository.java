package com.planovaai.backend.repository;

import com.planovaai.backend.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, String> {
    List<TeamMember> findByProjectId(String projectId);
    Optional<TeamMember> findByProjectIdAndUserEmail(String projectId, String email);
    boolean existsByProjectIdAndDeveloperEmail(String projectId, String email);
}
