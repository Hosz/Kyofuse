package com.hokyozu.kyofuse.communities.repository;

import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityRepository extends JpaRepository<Community, UUID> {
    boolean existsBySlug(String slug);

    @Override
    @EntityGraph(attributePaths = {"owner"})
    Optional<Community> findById(UUID id);

    @EntityGraph(attributePaths = {"owner"})
    Optional<Community> findBySlug(String slug);

    @EntityGraph(attributePaths = {"owner"})
    @Query("""
        SELECT c FROM Community c 
        WHERE c.status = :status 
          AND (LOWER(c.slug) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY
          CASE WHEN LOWER(c.slug) LIKE LOWER(CONCAT(:query, '%')) OR LOWER(c.name) LIKE LOWER(CONCAT(:query, '%')) THEN 0 ELSE 1 END,
          c.name ASC
    """)
    Page<Community> searchActiveCommunities(@Param("query") String query, @Param("status") CommunityStatus status, Pageable pageable);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    @EntityGraph(attributePaths = {"owner"})
    Page<Community> findAllByStatus(CommunityStatus communityStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"owner"})
    Page<Community> findAllByStatusAndNameContainingIgnoreCase(CommunityStatus communityStatus, String name, Pageable pageable);

    @EntityGraph(attributePaths = {"owner"})
    Optional<Community> findByTeamId(UUID teamId);

    List<Community> findAllByOwner(com.hokyozu.kyofuse.users.entity.User owner);

    @EntityGraph(attributePaths = {"owner"})
    @Query("""
        SELECT DISTINCT c FROM Community c
        LEFT JOIN CommunityMember cm ON cm.community = c AND cm.user.id = :userId AND cm.status = com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus.ACTIVE AND cm.role = com.hokyozu.kyofuse.communities.enums.CommunityMemberRole.ADMIN
        WHERE c.status = com.hokyozu.kyofuse.communities.enums.CommunityStatus.ACTIVE
          AND c.team IS NULL
          AND (c.owner.id = :userId OR cm.id IS NOT NULL)
        ORDER BY c.name ASC
    """)
    List<Community> findAvailableForTeam(@Param("userId") UUID userId);
}
