package com.hokyozu.kyofuse.communities.service;

import com.hokyozu.kyofuse.communities.dto.response.CommunityMemberResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberRole;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.communities.mapper.CommunityMemberMapper;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityMemberService {

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final UserFinder userFinder;
    private final UserChecker userChecker;
    private final GamerProfileFinder gamerProfileFinder;

    @Autowired
    @Lazy
    private CommunityService communityService;

    @Transactional
    public CommunityMemberResponse joinCommunity(UUID userId, UUID communityId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new NotFoundException("Community not found"));

        if (community.getStatus() == CommunityStatus.ARCHIVED) {
            throw new BadRequestException("Cannot join an archived community");
        }

        if (community.getVisibility() == CommunityVisibility.PRIVATE) {
            throw new ForbiddenException("This community requires an approved join request");
        }

        return addMember(user, community);
    }

    /**
     * Adiciona (ou readiciona) um membro à comunidade, sem checar visibilidade —
     * usado tanto por joinCommunity (depois do check de PUBLIC) quanto pela aprovação
     * de solicitação de entrada em comunidades PRIVATE (CommunityJoinRequestService),
     * onde a autorização já foi verificada por outro caminho.
     */
    @Transactional
    public CommunityMemberResponse addMember(User user, Community community) {
        CommunityMember communityMember = communityMemberRepository
                .findByUserIdAndCommunityId(user.getId(), community.getId())
                .orElse(null);

        if (communityMember == null) {
            communityMember = CommunityMemberMapper.toEntity(user, community);
            communityMemberRepository.save(communityMember);
            return CommunityMemberMapper.toResponse(communityMember, gamerProfileFinder.findProfileByUserId(user.getId()));
        }

        if (communityMember.getStatus() == CommunityMemberStatus.BANNED) {
            throw new ForbiddenException("User is banned from this community");
        }

        if (communityMember.getStatus() == CommunityMemberStatus.ACTIVE) {
            throw new BadRequestException("User is already a member of the community");
        }

        // community_members nunca é removida fisicamente: reaproveita a mesma linha
        // (LEFT, REMOVED ou KICKED) em vez de criar um novo registro.
        communityMember.setStatus(CommunityMemberStatus.ACTIVE);
        communityMember.setRole(CommunityMemberRole.MEMBER);
        communityMember.setJoinedAt(Instant.now());
        communityMember.setLeftAt(null);
        communityMember.setUpdatedAt(Instant.now());
        communityMemberRepository.save(communityMember);

        return CommunityMemberMapper.toResponse(communityMember, gamerProfileFinder.findProfileByUserId(user.getId()));
    }

    @Transactional(readOnly = true)
    public Page<CommunityMemberResponse> listCommunityMembers(UUID communityId, UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        if (!isActiveMember(userId, communityId)) {
            throw new ForbiddenException("User is not a member of the community");
        }

        Page<CommunityMember> members = communityMemberRepository.findByCommunityId(communityId, pageable);
        return members.map(member -> CommunityMemberMapper.toResponse(
                member,
                gamerProfileFinder.findProfileByUserId(member.getUser().getId())
        ));
    }

    @Transactional
    public void leaveCommunity(UUID userId, UUID communityId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        CommunityMember communityMember = communityMemberRepository.findByUserIdAndCommunityId(userId, communityId)
                .filter(member -> member.getStatus() == CommunityMemberStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("User is not a member of the community"));

        Community community = communityMember.getCommunity();
        if (community.getOwner().getId().equals(userId)) {
            List<CommunityMember> activeMembers = communityMemberRepository.findByCommunityAndStatus(community, CommunityMemberStatus.ACTIVE);
            boolean hasOtherActiveMembers = activeMembers.stream()
                    .anyMatch(m -> !m.getUser().getId().equals(userId));
            if (hasOtherActiveMembers) {
                throw new BadRequestException("O dono não pode sair da comunidade enquanto houver outros membros. Transfira a posse ou apague a comunidade.");
            }
            if (communityService != null) {
                communityService.deleteCommunity(userId, community.getId(), false);
            }
            return;
        }

        communityMember.setStatus(CommunityMemberStatus.LEFT);
        communityMember.setLeftAt(Instant.now());
        communityMember.setUpdatedAt(Instant.now());
        communityMemberRepository.save(communityMember);
    }

    @Transactional
    public void removeMember(UUID userId, UUID communityId, UUID memberId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new NotFoundException("Community not found"));

        if (!isOwnerOrStaff(user, community)) {
            throw new ForbiddenException("Only the community owner, an admin or a moderator can remove members");
        }

        if (memberId.equals(userId)) {
            throw new BadRequestException("Use the leave endpoint to remove yourself from the community");
        }

        CommunityMember communityMember = communityMemberRepository.findByUserIdAndCommunityId(memberId, communityId)
                .filter(member -> member.getStatus() == CommunityMemberStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Member not found in the community"));

        if (community.getOwner().getId().equals(memberId)) {
            throw new BadRequestException("Community owner cannot be removed from the community");
        }

        boolean isOwner = community.getOwner().getId().equals(userId);
        if (!isOwner) {
            CommunityMember actorMembership = communityMemberRepository.findByUserIdAndCommunityId(userId, communityId)
                    .filter(member -> member.getStatus() == CommunityMemberStatus.ACTIVE)
                    .orElseThrow(() -> new ForbiddenException("Actor is not an active member of the community"));

            CommunityMemberRole actorRole = actorMembership.getRole();
            CommunityMemberRole targetRole = communityMember.getRole();

            if (actorRole == CommunityMemberRole.MODERATOR) {
                if (targetRole == CommunityMemberRole.ADMIN) {
                    throw new ForbiddenException("Moderators cannot remove administrators");
                }
                if (targetRole == CommunityMemberRole.MODERATOR) {
                    throw new ForbiddenException("Moderators cannot remove other moderators");
                }
            }

            if (actorRole == CommunityMemberRole.ADMIN && targetRole == CommunityMemberRole.ADMIN) {
                throw new ForbiddenException("Admins cannot remove other administrators. Only the community owner can remove an admin.");
            }
        }

        communityMember.setStatus(CommunityMemberStatus.REMOVED);
        communityMember.setLeftAt(Instant.now());
        communityMember.setUpdatedAt(Instant.now());
        communityMemberRepository.save(communityMember);
    }

    @Transactional
    public CommunityMemberResponse joinCommunity(UUID userId, String communityIdentifier) {
        Community community = findCommunityByIdentifier(communityIdentifier);
        return joinCommunity(userId, community.getId());
    }

    @Transactional(readOnly = true)
    public Page<CommunityMemberResponse> listCommunityMembers(String communityIdentifier, UUID userId, Pageable pageable) {
        Community community = findCommunityByIdentifier(communityIdentifier);
        return listCommunityMembers(community.getId(), userId, pageable);
    }

    @Transactional
    public void leaveCommunity(UUID userId, String communityIdentifier) {
        Community community = findCommunityByIdentifier(communityIdentifier);
        leaveCommunity(userId, community.getId());
    }

    @Transactional
    public void removeMember(UUID userId, String communityIdentifier, UUID memberId) {
        Community community = findCommunityByIdentifier(communityIdentifier);
        removeMember(userId, community.getId(), memberId);
    }

    @Transactional
    public CommunityMemberResponse updateMemberRole(UUID actorId, String communityIdentifier, UUID memberId, CommunityMemberRole newRole) {
        User actor = userFinder.findProfileByUserId(actorId);
        userChecker.checkActive(actor);

        Community community = findCommunityByIdentifier(communityIdentifier);

        boolean isOwner = community.getOwner().getId().equals(actorId);
        if (!isOwner) {
            CommunityMember actorMember = communityMemberRepository.findByUserIdAndCommunityId(actorId, community.getId())
                    .filter(m -> m.getStatus() == CommunityMemberStatus.ACTIVE)
                    .orElseThrow(() -> new ForbiddenException("Você não é membro ativo desta comunidade."));
            if (actorMember.getRole() != CommunityMemberRole.ADMIN) {
                throw new ForbiddenException("Apenas o dono ou administradores podem alterar funções de membros.");
            }
            if (newRole == CommunityMemberRole.ADMIN) {
                throw new ForbiddenException("Apenas o dono da comunidade pode promover membros a Administrador.");
            }
        }

        if (community.getOwner().getId().equals(memberId)) {
            throw new BadRequestException("O papel do dono da comunidade não pode ser alterado.");
        }

        CommunityMember targetMember = communityMemberRepository.findByUserIdAndCommunityId(memberId, community.getId())
                .filter(m -> m.getStatus() == CommunityMemberStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Membro ativo não encontrado na comunidade."));

        targetMember.setRole(newRole);
        targetMember.setUpdatedAt(Instant.now());
        communityMemberRepository.save(targetMember);

        return CommunityMemberMapper.toResponse(targetMember, gamerProfileFinder.findProfileByUserId(memberId));
    }

    public Community findCommunityByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new NotFoundException("Comunidade não encontrada.");
        }
        try {
            return communityRepository.findById(UUID.fromString(identifier.trim()))
                    .filter(c -> c.getStatus() != CommunityStatus.ARCHIVED)
                    .orElseThrow(() -> new NotFoundException("Comunidade não encontrada."));
        } catch (IllegalArgumentException e) {
            return communityRepository.findBySlug(identifier.trim())
                    .filter(c -> c.getStatus() != CommunityStatus.ARCHIVED)
                    .orElseThrow(() -> new NotFoundException("Comunidade não encontrada: " + identifier));
        }
    }

    /**
     * Owner, Admin ou Moderador — mesma hierarquia documentada em 10.5 do doc.md,
     * usada tanto aqui (removeMember) quanto por CommunityJoinRequestService
     * (approve/reject de solicitações de entrada).
     */
    public boolean isOwnerOrStaff(User user, Community community) {
        if (community.getOwner().getId().equals(user.getId())) {
            return true;
        }

        return communityMemberRepository.findByUserIdAndCommunityId(user.getId(), community.getId())
                .filter(member -> member.getStatus() == CommunityMemberStatus.ACTIVE)
                .map(member -> member.getRole() == CommunityMemberRole.ADMIN
                        || member.getRole() == CommunityMemberRole.MODERATOR)
                .orElse(false);
    }

    private boolean isActiveMember(UUID userId, UUID communityId) {
        return communityMemberRepository.findByUserIdAndCommunityId(userId, communityId)
                .filter(member -> member.getStatus() == CommunityMemberStatus.ACTIVE)
                .isPresent();
    }
}
