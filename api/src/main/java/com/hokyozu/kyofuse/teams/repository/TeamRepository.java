package com.hokyozu.kyofuse.teams.repository;

import com.hokyozu.kyofuse.teams.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID>, JpaSpecificationExecutor<Team> {

    boolean existsBySlug(String slug);

    @Override
    @EntityGraph(attributePaths = {"owner"})
    Optional<Team> findById(UUID id);

    @EntityGraph(attributePaths = {"owner"})
    Optional<Team> findBySlug(String slug);

    @EntityGraph(attributePaths = {"owner"})
    @Query("""
        SELECT t FROM Team t 
        WHERE t.status IN :statuses 
          AND (LOWER(t.slug) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY
          CASE WHEN LOWER(t.slug) LIKE LOWER(CONCAT(:query, '%')) OR LOWER(t.name) LIKE LOWER(CONCAT(:query, '%')) THEN 0 ELSE 1 END,
          t.name ASC
    """)
    Page<Team> searchActiveTeams(@Param("query") String query, @Param("statuses") List<com.hokyozu.kyofuse.teams.enums.TeamStatus> statuses, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "owner")
    Page<Team> findAll(Specification<Team> specification, Pageable pageable);

    @EntityGraph(attributePaths = {"owner"})
    Page<Team> findByIdIn(Collection<UUID> ids, Pageable pageable);

    List<Team> findAllByOwner(com.hokyozu.kyofuse.users.entity.User owner);

    @EntityGraph(attributePaths = {"owner"})
    @Query("""
        SELECT DISTINCT t FROM Team t
        LEFT JOIN TeamMember tm ON tm.team = t AND tm.user.id = :userId AND tm.status = com.hokyozu.kyofuse.teams.enums.TeamMemberStatus.ACTIVE AND tm.memberType = com.hokyozu.kyofuse.teams.enums.TeamMemberType.MANAGER
        WHERE t.status != com.hokyozu.kyofuse.teams.enums.TeamStatus.INACTIVE
          AND (t.owner.id = :userId OR tm.id IS NOT NULL)
          AND NOT EXISTS (SELECT 1 FROM Community c WHERE c.team = t)
        ORDER BY t.name ASC
    """)
    List<Team> findAvailableForCommunity(@Param("userId") UUID userId);
}
