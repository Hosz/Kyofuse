package com.hokyozu.kyofuse.communities.repository;

import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommunityMemberRepository extends JpaRepository<CommunityMember, UUID> {
    Page<CommunityMember> findByCommunityId(UUID communityId, Pageable pageable);

    // Retorna o vínculo independentemente do status (ACTIVE, LEFT, REMOVED, KICKED, BANNED):
    // community_members nunca é removida fisicamente, então pode haver no máximo uma linha
    // por par (userId, communityId) para sempre. Quem chama decide o que fazer com o status.
    Optional<CommunityMember> findByUserIdAndCommunityId(UUID userId, UUID communityId);
}
