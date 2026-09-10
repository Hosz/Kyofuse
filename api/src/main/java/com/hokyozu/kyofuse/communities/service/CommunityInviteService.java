package com.hokyozu.kyofuse.communities.service;

import com.hokyozu.kyofuse.communities.dto.request.CommunityInviteCancelRequest;
import com.hokyozu.kyofuse.communities.dto.request.CommunityInviteRequest;
import com.hokyozu.kyofuse.communities.dto.response.CommunityInviteResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityInvite;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityInviteStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.mapper.CommunityInviteMapper;
import com.hokyozu.kyofuse.communities.repository.CommunityInviteRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.notifications.dto.request.CreateNotificationRequest;
import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.notifications.service.NotificationService;
import com.hokyozu.kyofuse.relationships.shared.validator.BlockValidator;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityInviteService {

    private final CommunityInviteRepository communityInviteRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final CommunityMemberService communityMemberService;
    private final UserFinder userFinder;
    private final UserChecker userChecker;
    private final NotificationService notificationService;
    private final BlockValidator blockValidator;

    @Transactional
    public CommunityInviteResponse inviteUser(UUID senderId, String communityIdentifier, String receiverUsername, CommunityInviteRequest request) {
        User sender = userFinder.findProfileByUserId(senderId);
        User receiver = userFinder.findProfileByUsername(receiverUsername);
        Community community = communityMemberService.findCommunityByIdentifier(communityIdentifier);

        userChecker.checkActive(sender);
        userChecker.checkActive(receiver);

        if (community.getStatus() == CommunityStatus.ARCHIVED) {
            throw new BadRequestException("Não é possível convidar para uma comunidade arquivada.");
        }

        boolean isMemberOrOwner = community.getOwner().getId().equals(senderId)
                || communityMemberService.isActiveMember(senderId, community.getId());
        if (!isMemberOrOwner) {
            throw new ForbiddenException("Apenas membros da comunidade podem enviar convites.");
        }

        if (senderId.equals(receiver.getId())) {
            throw new BadRequestException("Você não pode convidar a si mesmo.");
        }

        if (receiver.getId().equals(community.getOwner().getId())) {
            throw new BadRequestException("O usuário já é o dono da comunidade.");
        }

        blockValidator.validate(sender, receiver);

        if (communityMemberService.isActiveMember(receiver.getId(), community.getId())) {
            throw new BadRequestException("O usuário já é membro desta comunidade.");
        }

        CommunityMember existingMember = communityMemberRepository.findByUserIdAndCommunityId(receiver.getId(), community.getId()).orElse(null);
        if (existingMember != null && existingMember.getStatus() == CommunityMemberStatus.BANNED) {
            throw new ForbiddenException("O usuário está banido desta comunidade.");
        }

        if (communityInviteRepository.existsByCommunityAndReceiverAndStatus(community, receiver, CommunityInviteStatus.PENDING)) {
            throw new BadRequestException("Já existe um convite pendente para este usuário nesta comunidade.");
        }

        CommunityInvite invite = CommunityInviteMapper.toEntity(community, sender, receiver, request);
        communityInviteRepository.save(invite);

        notificationService.createNotification(
                CreateNotificationRequest.builder()
                        .recipient(receiver)
                        .actor(sender)
                        .type(NotificationType.COMMUNITY_INVITE_RECEIVED)
                        .title("Novo convite.")
                        .message(sender.getUsername() + " convidou você para se juntar à comunidade " + community.getName() + ".")
                        .targetType(NotificationTargetType.COMMUNITY_INVITE)
                        .targetId(invite.getId())
                        .metadata(Map.of(
                                "CommunityName", community.getName(),
                                "CommunitySlug", community.getSlug()
                        ))
                        .build()
        );

        return CommunityInviteMapper.toResponse(invite);
    }

    @Transactional(readOnly = true)
    public Page<CommunityInviteResponse> listInvites(UUID userId, String communityIdentifier, Pageable pageable, CommunityInviteStatus status) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Community community = communityMemberService.findCommunityByIdentifier(communityIdentifier);

        boolean isMemberOrOwner = community.getOwner().getId().equals(userId)
                || communityMemberService.isActiveMember(userId, community.getId());
        if (!isMemberOrOwner) {
            throw new ForbiddenException("Apenas membros podem visualizar convites.");
        }

        if (status != null) {
            return communityInviteRepository.findAllByCommunityAndStatus(community, pageable, status)
                    .map(CommunityInviteMapper::toResponse);
        }
        return communityInviteRepository.findAllByCommunity(community, pageable)
                .map(CommunityInviteMapper::toResponse);
    }

    @Transactional
    public void acceptInvite(UUID userId, UUID inviteId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        CommunityInvite invite = communityInviteRepository.findById(inviteId)
                .orElseThrow(() -> new NotFoundException("Convite não encontrado."));

        if (!invite.getReceiver().getId().equals(userId)) {
            throw new ForbiddenException("Você não tem permissão para aceitar este convite.");
        }

        if (invite.getStatus() == CommunityInviteStatus.ACCEPTED) {
            throw new BadRequestException("Esse convite já foi aceito.");
        }
        if (invite.getStatus() == CommunityInviteStatus.DECLINED) {
            throw new BadRequestException("Esse convite já foi recusado.");
        }
        if (invite.getStatus() == CommunityInviteStatus.CANCELED) {
            throw new BadRequestException("Esse convite foi cancelado.");
        }
        if (invite.getStatus() != CommunityInviteStatus.PENDING) {
            throw new BadRequestException("Este convite não está mais pendente.");
        }

        Community community = invite.getCommunity();
        if (community.getStatus() == CommunityStatus.ARCHIVED) {
            throw new BadRequestException("Não é possível entrar em uma comunidade arquivada.");
        }

        communityMemberService.addMember(user, community);

        invite.setStatus(CommunityInviteStatus.ACCEPTED);
        invite.setRespondedAt(Instant.now());
        communityInviteRepository.save(invite);

        notificationService.markCommunityInviteAsAccepted(userId, invite.getId());

        notificationService.createNotification(
                CreateNotificationRequest.builder()
                        .recipient(invite.getSender())
                        .actor(user)
                        .type(NotificationType.COMMUNITY_INVITE_ACCEPTED)
                        .title("Convite aceito.")
                        .message(user.getUsername() + " aceitou o convite para a comunidade " + community.getName() + ".")
                        .targetType(NotificationTargetType.COMMUNITY_INVITE)
                        .targetId(invite.getId())
                        .metadata(Map.of(
                                "CommunityName", community.getName(),
                                "CommunitySlug", community.getSlug()
                        ))
                        .build()
        );
    }

    @Transactional
    public void declineInvite(UUID userId, UUID inviteId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        CommunityInvite invite = communityInviteRepository.findById(inviteId)
                .orElseThrow(() -> new NotFoundException("Convite não encontrado."));

        if (!invite.getReceiver().getId().equals(userId)) {
            throw new ForbiddenException("Você não tem permissão para recusar este convite.");
        }

        if (invite.getStatus() != CommunityInviteStatus.PENDING) {
            throw new BadRequestException("Este convite não está mais pendente.");
        }

        invite.setStatus(CommunityInviteStatus.DECLINED);
        invite.setRespondedAt(Instant.now());
        communityInviteRepository.save(invite);

        notificationService.markCommunityInviteAsDeclined(userId, invite.getId());

        notificationService.createNotification(
                CreateNotificationRequest.builder()
                        .recipient(invite.getSender())
                        .actor(user)
                        .type(NotificationType.COMMUNITY_INVITE_DECLINED)
                        .title("Convite recusado.")
                        .message(user.getUsername() + " recusou o convite para a comunidade " + invite.getCommunity().getName() + ".")
                        .targetType(NotificationTargetType.COMMUNITY_INVITE)
                        .targetId(invite.getId())
                        .metadata(Map.of(
                                "CommunityName", invite.getCommunity().getName(),
                                "CommunitySlug", invite.getCommunity().getSlug()
                        ))
                        .build()
        );
    }

    @Transactional
    public void cancelInvite(UUID userId, UUID inviteId, CommunityInviteCancelRequest request) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        CommunityInvite invite = communityInviteRepository.findById(inviteId)
                .orElseThrow(() -> new NotFoundException("Convite não encontrado."));

        boolean isSender = invite.getSender().getId().equals(userId);
        boolean isStaffOrOwner = communityMemberService.isOwnerOrStaff(user, invite.getCommunity());
        if (!isSender && !isStaffOrOwner) {
            throw new ForbiddenException("Você não tem permissão para cancelar este convite.");
        }

        if (invite.getStatus() != CommunityInviteStatus.PENDING) {
            throw new BadRequestException("Este convite não está mais pendente.");
        }

        invite.setStatus(CommunityInviteStatus.CANCELED);
        invite.setCanceledBy(user);
        invite.setCancellationReason(request != null ? request.cancellationReason() : null);
        invite.setCanceledAt(Instant.now());
        communityInviteRepository.save(invite);

        notificationService.markCommunityInviteAsCanceled(invite.getReceiver().getId(), invite.getId());
    }
}
