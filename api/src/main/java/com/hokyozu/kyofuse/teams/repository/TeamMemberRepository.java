package com.hokyozu.kyofuse.teams.repository;

import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamMember;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    boolean existsByTeamAndUser(Team team, User userInvited);

    TeamMember findByTeamAndUser(Team team, User userEdited);

    List<TeamMember> findByMemberTypeAndAssignmentDueAtBefore(TeamMemberType memberType, Instant instant);

    Page<TeamMember> findByTeam(Team team, Pageable pageable);
}
