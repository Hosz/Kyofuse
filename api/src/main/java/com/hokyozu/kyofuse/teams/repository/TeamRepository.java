package com.hokyozu.kyofuse.teams.repository;

import com.hokyozu.kyofuse.teams.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.UUID;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, UUID>, JpaSpecificationExecutor<Team> {

    boolean existsBySlug(String slug);
    Optional<Team> findBySlug(String slug);
    @org.springframework.data.jpa.repository.Query("""
        SELECT t FROM Team t 
        WHERE t.status IN :statuses 
          AND (LOWER(t.slug) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY
          CASE WHEN LOWER(t.slug) LIKE LOWER(CONCAT(:query, '%')) OR LOWER(t.name) LIKE LOWER(CONCAT(:query, '%')) THEN 0 ELSE 1 END,
          t.name ASC
    """)
    org.springframework.data.domain.Page<Team> searchActiveTeams(@org.springframework.data.repository.query.Param("query") String query, @org.springframework.data.repository.query.Param("statuses") java.util.List<com.hokyozu.kyofuse.teams.enums.TeamStatus> statuses, org.springframework.data.domain.Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "owner")
    Page<Team> findAll(Specification<Team> specification, Pageable pageable);

    Page<Team> findByIdIn(Collection<UUID> ids, Pageable pageable);

    java.util.List<Team> findAllByOwner(com.hokyozu.kyofuse.users.entity.User owner);
}
