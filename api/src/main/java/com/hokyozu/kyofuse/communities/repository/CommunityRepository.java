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

    boolean existsBySlugAndIdNot(String slug, UUID id);

    Page<Community> findAllByStatus(CommunityStatus communityStatus, Pageable pageable);

    Page<Community> findAllByStatusAndNameContainingIgnoreCase(CommunityStatus communityStatus, String name, Pageable pageable);

    Optional<Community> findByTeamId(UUID teamId);
}
