package com.hokyozu.kyofuse.communities.repository;

import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommunityRepository extends JpaRepository<Community, UUID> {
    boolean existsBySlug(String slug);
    Optional<Community> findBySlug(String slug);
    @org.springframework.data.jpa.repository.Query("""
        SELECT c FROM Community c 
        WHERE c.status = :status 
          AND (LOWER(c.slug) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY
          CASE WHEN LOWER(c.slug) LIKE LOWER(CONCAT(:query, '%')) OR LOWER(c.name) LIKE LOWER(CONCAT(:query, '%')) THEN 0 ELSE 1 END,
          c.name ASC
    """)
    org.springframework.data.domain.Page<Community> searchActiveCommunities(@org.springframework.data.repository.query.Param("query") String query, @org.springframework.data.repository.query.Param("status") com.hokyozu.kyofuse.communities.enums.CommunityStatus status, org.springframework.data.domain.Pageable pageable);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    Page<Community> findAllByStatus(CommunityStatus communityStatus, Pageable pageable);

    Page<Community> findAllByStatusAndNameContainingIgnoreCase(CommunityStatus communityStatus, String name, Pageable pageable);

    Optional<Community> findByTeamId(UUID teamId);

    java.util.List<Community> findAllByOwner(com.hokyozu.kyofuse.users.entity.User owner);
}
