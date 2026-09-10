package com.hokyozu.kyofuse.invites.repository;

import com.hokyozu.kyofuse.invites.entity.TeamInvite;
import com.hokyozu.kyofuse.invites.enums.TeamInviteStatus;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TeamInviteRepository extends JpaRepository<TeamInvite, UUID> {
    boolean existsByTeamAndReceiverAndStatus(Team team, User receiver, TeamInviteStatus teamInviteStatus);

    @EntityGraph(attributePaths = {"team", "sender", "receiver"})
    Page<TeamInvite> findAllByTeamAndStatus(Team team, Pageable pageable, TeamInviteStatus status);

    @EntityGraph(attributePaths = {"team", "sender", "receiver"})
    Page<TeamInvite> findAllByTeam(Team team, Pageable pageable);

    @EntityGraph(attributePaths = {"team", "sender", "receiver"})
    TeamInvite findByTeamAndReceiver(Team team, User userInvited);

    void deleteByTeam(Team team);
}
