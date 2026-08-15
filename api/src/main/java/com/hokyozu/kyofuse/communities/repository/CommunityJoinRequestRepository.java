package com.hokyozu.kyofuse.communities.repository;

import com.hokyozu.kyofuse.communities.entity.CommunityJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommunityJoinRequestRepository extends JpaRepository<CommunityJoinRequest, UUID> {
    boolean existsByCommunityIdAndRequesterId(UUID communityId, UUID requesterId);
}
