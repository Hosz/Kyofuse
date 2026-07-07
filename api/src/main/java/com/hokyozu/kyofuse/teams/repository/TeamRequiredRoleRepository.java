package com.hokyozu.kyofuse.teams.repository;

import com.hokyozu.kyofuse.teams.entity.TeamRequiredRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TeamRequiredRoleRepository extends JpaRepository<TeamRequiredRole, UUID> {
    List<TeamRequiredRole> findByTeamId(UUID teamId);

    List<TeamRequiredRole> findByTeamIdIn(List<UUID> teamIds);
}
