package com.hokyozu.kyofuse.communities.repository;

import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityMemberRepository extends JpaRepository<CommunityMember, UUID> {
    Page<CommunityMember> findByCommunityId(UUID communityId, Pageable pageable);

    // Retorna o vínculo independentemente do status (ACTIVE, LEFT, REMOVED, KICKED, BANNED):
    // community_members nunca é removida fisicamente, então pode haver no máximo uma linha
    // por par (userId, communityId) para sempre. Quem chama decide o que fazer com o status.
    Optional<CommunityMember> findByUserIdAndCommunityId(UUID userId, UUID communityId);

    Page<CommunityMember> findByUserAndStatus(User user, CommunityMemberStatus status, Pageable pageable);

    Page<CommunityMember> findByUserIdAndStatus(UUID userId, CommunityMemberStatus status, Pageable pageable);

    Optional<CommunityMember> findByCommunityAndUser(Community community, User user);

    // Sem paginação de propósito: usado pra notificar todos os membros ativos de uma
    // comunidade quando alguém manda uma mensagem, não pra exibir uma listagem.
    List<CommunityMember> findByCommunityAndStatus(Community community, CommunityMemberStatus status);
}
