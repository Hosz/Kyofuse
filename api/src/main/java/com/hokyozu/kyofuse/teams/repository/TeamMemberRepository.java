package com.hokyozu.kyofuse.teams.repository;

import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamMember;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    boolean existsByTeamAndUser(Team team, User userInvited);

    @EntityGraph(attributePaths = {"user", "team"})
    TeamMember findByTeamAndUser(Team team, User userEdited);

    List<TeamMember> findByMemberTypeAndAssignmentDueAtBefore(TeamMemberType memberType, Instant instant);

    @EntityGraph(attributePaths = {"user", "team"})
    Page<TeamMember> findByTeam(Team team, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "team"})
    List<TeamMember> findByTeam(Team team);

    Boolean existsByUser(User author);

    @Query("""
        SELECT COUNT(tm1) > 0
        FROM TeamMember tm1
        JOIN TeamMember tm2
            ON tm1.team = tm2.team
        WHERE tm1.user = :first
          AND tm2.user = :second
          AND tm1.status = ACTIVE
          AND tm2.status = ACTIVE
    """)
    boolean areTeammates(User first, User second);

    @EntityGraph(attributePaths = {"user", "team"})
    List<TeamMember> findByUser(User user);
}
