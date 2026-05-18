package com.planovaai.backend.repository;

import com.planovaai.backend.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, String> {
    List<TeamMember> findByProjectId(String projectId);
    List<TeamMember> findByUserIdOrDeveloperEmail(String userId, String email);
    Optional<TeamMember> findByProjectIdAndDeveloperEmail(String projectId, String email);
}
