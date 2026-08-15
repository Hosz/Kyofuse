package com.hokyozu.kyofuse.communities.repository;

import com.hokyozu.kyofuse.communities.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommunityRepository extends JpaRepository<Community, UUID> {
    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);
}
