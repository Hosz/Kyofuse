package com.hokyozu.kyofuse.teams.repository;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.entity.TeamRequiredRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamRequiredRoleRepository extends JpaRepository<TeamRequiredRole, UUID> {
    List<TeamRequiredRole> findByTeamId(UUID teamId);

    List<TeamRequiredRole> findByTeamIdIn(List<UUID> teamIds);

    List<TeamRequiredRole> findByTeamIdAndRoleName(UUID teamId, PlayerRole roleName);

    void deleteByTeamId(UUID teamId);
}
