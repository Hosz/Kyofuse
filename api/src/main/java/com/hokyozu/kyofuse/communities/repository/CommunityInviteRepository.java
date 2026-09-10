package com.hokyozu.kyofuse.communities.repository;

import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityInvite;
import com.hokyozu.kyofuse.communities.enums.CommunityInviteStatus;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommunityInviteRepository extends JpaRepository<CommunityInvite, UUID> {

    boolean existsByCommunityAndReceiverAndStatus(Community community, User receiver, CommunityInviteStatus status);

    Page<CommunityInvite> findAllByCommunity(Community community, Pageable pageable);

    Page<CommunityInvite> findAllByCommunityAndStatus(Community community, Pageable pageable, CommunityInviteStatus status);

    Page<CommunityInvite> findAllByReceiverAndStatus(User receiver, CommunityInviteStatus status, Pageable pageable);

    Optional<CommunityInvite> findByIdAndReceiver(UUID id, User receiver);
}
