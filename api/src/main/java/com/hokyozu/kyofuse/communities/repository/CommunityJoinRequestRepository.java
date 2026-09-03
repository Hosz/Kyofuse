package com.hokyozu.kyofuse.communities.repository;

import com.hokyozu.kyofuse.communities.entity.CommunityJoinRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommunityJoinRequestRepository extends JpaRepository<CommunityJoinRequest, UUID> {
    boolean existsByCommunityIdAndRequesterId(UUID communityId, UUID requesterId);

    @EntityGraph(attributePaths = {"community", "requester"})
    Page<CommunityJoinRequest> findByCommunityId(UUID communityId, Pageable pageable);
}
