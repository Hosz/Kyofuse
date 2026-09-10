package com.hokyozu.kyofuse.communities.mapper;

import com.hokyozu.kyofuse.communities.dto.response.CommunityMemberResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberRole;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;

public class CommunityMemberMapper {
    public static CommunityMember toEntity(User user, Community community) {
        return CommunityMember.builder()
                .community(community)
                .user(user)
                .role(CommunityMemberRole.MEMBER)
                .status(CommunityMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * O dono da comunidade também recebe um vínculo próprio em community_members, como
     * ADMIN, para aparecer na listagem de membros e nas comunidades de que participa
     * (ver doc.md 10.5). Sem essa linha ele ficaria de fora das duas listagens, que são
     * derivadas exclusivamente de community_members.
     */
    public static CommunityMember toOwnerEntity(User owner, Community community) {
        return CommunityMember.builder()
                .community(community)
                .user(owner)
                .role(CommunityMemberRole.ADMIN)
                .status(CommunityMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static CommunityMember toAdminEntity(User user, Community community) {
        return CommunityMember.builder()
                .community(community)
                .user(user)
                .role(CommunityMemberRole.ADMIN)
                .status(CommunityMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * gamerProfile pode vir null quando o chamador não precisa exibir a identidade
     * visual do membro (ex.: resposta de entrar na comunidade, onde o usuário é o
     * próprio solicitante) — nesse caso nickname e avatar saem nulos.
     */
    public static CommunityMemberResponse toResponse(CommunityMember communityMember, GamerProfile gamerProfile) {
        return new CommunityMemberResponse(
                communityMember.getId(),
                communityMember.getCommunity().getId(),
                communityMember.getCommunity().getName(),
                communityMember.getCommunity().getSlug(),
                communityMember.getUser().getId(),
                communityMember.getUser().getUsername(),
                gamerProfile != null ? gamerProfile.getNickname() : null,
                gamerProfile != null ? gamerProfile.getAvatarUrl() : null,
                communityMember.getRole(),
                communityMember.getStatus(),
                communityMember.getJoinedAt(),
                communityMember.getLeftAt(),
                communityMember.getCreatedAt(),
                communityMember.getUpdatedAt()
        );
    }
}
