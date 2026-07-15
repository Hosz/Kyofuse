package com.hokyozu.kyofuse.invites.repository;

import com.hokyozu.kyofuse.invites.entity.TeamInvite;
import com.hokyozu.kyofuse.invites.enums.TeamInviteStatus;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TeamInviteRepository extends JpaRepository<TeamInvite, UUID> {
    boolean existsByTeamAndReceiverAndStatus(Team team, User receiver, TeamInviteStatus teamInviteStatus);

    Page<TeamInvite> findAllByTeamAndStatus(Team team, Pageable pageable, TeamInviteStatus status);

    Page<TeamInvite> findAllByTeam(Team team, Pageable pageable);
}
