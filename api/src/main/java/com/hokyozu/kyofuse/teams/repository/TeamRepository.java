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

public interface TeamRepository extends JpaRepository<Team, UUID>, JpaSpecificationExecutor<Team> {

    boolean existsBySlug(String slug);

    @Override
    @EntityGraph(attributePaths = "owner")
    Page<Team> findAll(Specification<Team> specification, Pageable pageable);

    Page<Team> findByIdIn(Collection<UUID> ids, Pageable pageable);

    java.util.List<Team> findAllByOwner(com.hokyozu.kyofuse.users.entity.User owner);
}
