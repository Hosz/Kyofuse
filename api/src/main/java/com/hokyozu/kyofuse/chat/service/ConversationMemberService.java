package com.hokyozu.kyofuse.chat.service;

import com.hokyozu.kyofuse.chat.dto.response.ConversationMemberResponse;
import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.entity.ConversationMember;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberRole;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberStatus;
import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.chat.mapper.ConversationMemberMapper;
import com.hokyozu.kyofuse.chat.repository.ConversationMemberRepository;
import com.hokyozu.kyofuse.chat.repository.ConversationRepository;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationMemberService {

    private final UserFinder userFinder;
    private final UserChecker userChecker;
    private final GamerProfileFinder gamerProfileFinder;
    private final BlockValidator blockValidator;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    @Transactional
    public void addMemberToConversation(UUID conversationId, UUID memberId, UUID userId) {
        User actor = userFinder.findProfileByUserId(userId);
        User member = userFinder.findProfileByUserId(memberId);
        userChecker.checkActive(actor);
        userChecker.checkActive(member);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        if (conversation.getType() != ConversationType.GROUP) {
            throw new BadRequestException("Only GROUP conversations support adding members.");
        }

        ConversationMember actorMembership = conversationMemberRepository.findByConversationAndUser(conversation, actor)
                .filter(m -> m.getStatus() == ConversationMemberStatus.ACTIVE)
                .orElseThrow(() -> new ForbiddenException("User is not an active member of the conversation"));

        blockValidator.validate(actor, member);

        ConversationMember existing = conversationMemberRepository.findByConversationAndUser(conversation, member)
                .orElse(null);

        if (existing != null) {
            if (existing.getStatus() == ConversationMemberStatus.ACTIVE) {
                throw new BadRequestException("Member is already part of the conversation");
            }

            // conversation_members nunca é removida fisicamente: reaproveita a mesma
            // linha (LEFT, REMOVED ou KICKED) em vez de criar um novo registro.
            existing.setStatus(ConversationMemberStatus.ACTIVE);
            existing.setRole(ConversationMemberRole.MEMBER);
            existing.setJoinedAt(Instant.now());
            existing.setLeftAt(null);
            existing.setUpdatedAt(Instant.now());
            conversationMemberRepository.save(existing);
            return;
        }

        ConversationMember newConversationMember = ConversationMemberMapper.toGroupMember(conversation, member);
        conversationMemberRepository.save(newConversationMember);
    }

    @Transactional(readOnly = true)
    public Page<ConversationMemberResponse> listConversationMembers(UUID conversationId, UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        conversationMemberRepository.findByConversationAndUser(conversation, user)
                .filter(m -> m.getStatus() == ConversationMemberStatus.ACTIVE)
                .orElseThrow(() -> new ForbiddenException("User is not an active member of the conversation"));

        Page<ConversationMember> members = conversationMemberRepository.findByConversation(conversation, pageable);

        List<UUID> userIds = members.getContent().stream()
                .map(m -> m.getUser().getId())
                .distinct()
                .toList();

        Map<UUID, GamerProfile> profileMap = userIds.isEmpty()
                ? Map.of()
                : gamerProfileFinder.findAllByUserIds(userIds).stream()
                  .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity(), (a, b) -> a));

        return members.map(member -> ConversationMemberMapper.toResponse(
                    member,
                    profileMap.get(member.getUser().getId())
            ));
    }

    @Transactional
    public void promoteMemberToAdmin(UUID conversationId, UUID memberId, UUID userId) {
        User actor = userFinder.findProfileByUserId(userId);
        User member = userFinder.findProfileByUserId(memberId);
        userChecker.checkActive(actor);
        userChecker.checkActive(member);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        ConversationMember actorMembership = conversationMemberRepository.findByConversationAndUser(conversation, actor)
                .filter(m -> m.getStatus() == ConversationMemberStatus.ACTIVE)
                .orElseThrow(() -> new ForbiddenException("User is not an active member of the conversation"));

        if (actorMembership.getRole() != ConversationMemberRole.ADMIN) {
            throw new ForbiddenException("User does not have permission to promote members to admin");
        }

        ConversationMember conversationMember = conversationMemberRepository.findByConversationAndUser(conversation, member)
                .filter(m -> m.getStatus() == ConversationMemberStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Member is not an active participant of the conversation"));

        if (conversationMember.getRole() == ConversationMemberRole.ADMIN) {
            throw new BadRequestException("Member is already an admin");
        }

        conversationMember.setRole(ConversationMemberRole.ADMIN);
        conversationMember.setUpdatedAt(Instant.now());
        conversationMemberRepository.save(conversationMember);
    }

    @Transactional
    public void demoteAdminToMember(UUID conversationId, UUID memberId, UUID userId) {
        User actor = userFinder.findProfileByUserId(userId);
        User member = userFinder.findProfileByUserId(memberId);
        userChecker.checkActive(actor);
        userChecker.checkActive(member);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        ConversationMember actorMembership = conversationMemberRepository.findByConversationAndUser(conversation, actor)
                .filter(m -> m.getStatus() == ConversationMemberStatus.ACTIVE)
                .orElseThrow(() -> new ForbiddenException("User is not an active member of the conversation"));

        if (actorMembership.getRole() != ConversationMemberRole.ADMIN) {
            throw new ForbiddenException("User does not have permission to demote members from admin");
        }

        ConversationMember conversationMember = conversationMemberRepository.findByConversationAndUser(conversation, member)
                .filter(m -> m.getStatus() == ConversationMemberStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Member is not an active participant of the conversation"));

        if (conversationMember.getRole() != ConversationMemberRole.ADMIN) {
            throw new BadRequestException("Member is not an admin");
        }

        if (conversationMember.getUser().getId().equals(conversation.getCreatedBy().getId())) {
            throw new ForbiddenException("Cannot demote the creator of the conversation");
        }

        conversationMember.setRole(ConversationMemberRole.MEMBER);
        conversationMember.setUpdatedAt(Instant.now());
        conversationMemberRepository.save(conversationMember);
    }

    @Transactional
    public void removeMemberFromConversation(UUID conversationId, UUID memberId, UUID userId) {
        User actor = userFinder.findProfileByUserId(userId);
        User member = userFinder.findProfileByUserId(memberId);
        userChecker.checkActive(actor);
        userChecker.checkActive(member);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        ConversationMember actorMembership = conversationMemberRepository.findByConversationAndUser(conversation, actor)
                .filter(m -> m.getStatus() == ConversationMemberStatus.ACTIVE)
                .orElseThrow(() -> new ForbiddenException("User is not an active member of the conversation"));

        if (actorMembership.getRole() != ConversationMemberRole.ADMIN) {
            throw new ForbiddenException("User does not have permission to remove members from the conversation");
        }

        ConversationMember conversationMember = conversationMemberRepository.findByConversationAndUser(conversation, member)
                .filter(m -> m.getStatus() == ConversationMemberStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Member is not an active participant of the conversation"));

        if (conversationMember.getUser().getId().equals(conversation.getCreatedBy().getId())) {
            throw new ForbiddenException("Cannot remove the creator of the conversation");
        }

        if (conversationMember.getRole() == ConversationMemberRole.ADMIN) {
            throw new ForbiddenException("Cannot remove an administrator of the conversation");
        }

        conversationMember.setStatus(ConversationMemberStatus.REMOVED);
        conversationMember.setLeftAt(Instant.now());
        conversationMember.setUpdatedAt(Instant.now());
        conversationMemberRepository.save(conversationMember);
    }

    @Transactional
    public void leaveConversation(UUID conversationId, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        ConversationMember conversationMember = conversationMemberRepository.findByConversationAndUser(conversation, user)
                .filter(m -> m.getStatus() == ConversationMemberStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("User is not an active member of the conversation"));

        if (conversation.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("The creator of the conversation cannot leave it");
        }

        conversationMember.setStatus(ConversationMemberStatus.LEFT);
        conversationMember.setLeftAt(Instant.now());
        conversationMember.setUpdatedAt(Instant.now());
        conversationMemberRepository.save(conversationMember);
    }
}
