package com.hokyozu.kyofuse.chat.service;

import com.hokyozu.kyofuse.chat.dto.response.ConversationMemberResponse;
import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.entity.ConversationMember;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberRole;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberStatus;
import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.chat.repository.ConversationMemberRepository;
import com.hokyozu.kyofuse.chat.repository.ConversationRepository;
import com.hokyozu.kyofuse.relationships.shared.validator.BlockValidator;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationMemberServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private BlockValidator blockValidator;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationMemberRepository conversationMemberRepository;

    @Spy
    private UserChecker userChecker = new UserChecker();

    @InjectMocks
    private ConversationMemberService conversationMemberService;

    @Test
    void addMemberToConversationCreatesNewMemberWhenNoPriorRowExists() {
        User admin = activeUser("admin");
        User newMember = activeUser("newcomer");
        Conversation conversation = groupConversation(admin);
        ConversationMember adminMembership = membership(conversation, admin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(newMember.getId())).thenReturn(newMember);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, admin)).thenReturn(Optional.of(adminMembership));
        when(conversationMemberRepository.findByConversationAndUser(conversation, newMember)).thenReturn(Optional.empty());
        when(conversationMemberRepository.save(any(ConversationMember.class))).thenAnswer(inv -> inv.getArgument(0));

        conversationMemberService.addMemberToConversation(conversation.getId(), newMember.getId(), admin.getId());

        ArgumentCaptor<ConversationMember> captor = ArgumentCaptor.forClass(ConversationMember.class);
        verify(conversationMemberRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(newMember);
        assertThat(captor.getValue().getStatus()).isEqualTo(ConversationMemberStatus.ACTIVE);
        assertThat(captor.getValue().getRole()).isEqualTo(ConversationMemberRole.MEMBER);
        verify(blockValidator).validate(admin, newMember);
    }

    @Test
    void addMemberToConversationReactivatesFormerMemberInsteadOfCreatingNewRow() {
        User admin = activeUser("admin");
        User former = activeUser("former");
        Conversation conversation = groupConversation(admin);
        ConversationMember adminMembership = membership(conversation, admin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);
        ConversationMember leftMembership = membership(conversation, former, ConversationMemberRole.MEMBER, ConversationMemberStatus.LEFT);

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(former.getId())).thenReturn(former);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, admin)).thenReturn(Optional.of(adminMembership));
        when(conversationMemberRepository.findByConversationAndUser(conversation, former)).thenReturn(Optional.of(leftMembership));
        when(conversationMemberRepository.save(leftMembership)).thenReturn(leftMembership);

        conversationMemberService.addMemberToConversation(conversation.getId(), former.getId(), admin.getId());

        verify(conversationMemberRepository).save(leftMembership);
        assertThat(leftMembership.getStatus()).isEqualTo(ConversationMemberStatus.ACTIVE);
        assertThat(leftMembership.getLeftAt()).isNull();
    }

    @Test
    void addMemberToConversationRejectsWhenAlreadyActive() {
        User admin = activeUser("admin");
        User member = activeUser("member");
        Conversation conversation = groupConversation(admin);
        ConversationMember adminMembership = membership(conversation, admin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);
        ConversationMember activeMembership = membership(conversation, member, ConversationMemberRole.MEMBER, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(member.getId())).thenReturn(member);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, admin)).thenReturn(Optional.of(adminMembership));
        when(conversationMemberRepository.findByConversationAndUser(conversation, member)).thenReturn(Optional.of(activeMembership));

        assertThatThrownBy(() -> conversationMemberService.addMemberToConversation(conversation.getId(), member.getId(), admin.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Member is already part of the conversation");

        verify(conversationMemberRepository, never()).save(any());
    }

    @Test
    void addMemberToConversationRejectsNonAdminActor() {
        User actor = activeUser("actor");
        User newMember = activeUser("newcomer");
        Conversation conversation = groupConversation(activeUser("creator"));
        ConversationMember actorMembership = membership(conversation, actor, ConversationMemberRole.MEMBER, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(actor.getId())).thenReturn(actor);
        when(userFinder.findProfileByUserId(newMember.getId())).thenReturn(newMember);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, actor)).thenReturn(Optional.of(actorMembership));

        assertThatThrownBy(() -> conversationMemberService.addMemberToConversation(conversation.getId(), newMember.getId(), actor.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only an admin can add members to the conversation");
    }

    @Test
    void addMemberToConversationRejectsInactiveActor() {
        User actor = activeUser("actor");
        User newMember = activeUser("newcomer");
        Conversation conversation = groupConversation(activeUser("creator"));
        ConversationMember leftActorMembership = membership(conversation, actor, ConversationMemberRole.MEMBER, ConversationMemberStatus.LEFT);

        when(userFinder.findProfileByUserId(actor.getId())).thenReturn(actor);
        when(userFinder.findProfileByUserId(newMember.getId())).thenReturn(newMember);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, actor)).thenReturn(Optional.of(leftActorMembership));

        assertThatThrownBy(() -> conversationMemberService.addMemberToConversation(conversation.getId(), newMember.getId(), actor.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User is not an active member of the conversation");
    }

    @Test
    void addMemberToConversationRejectsNonGroupConversation() {
        User actor = activeUser("actor");
        User newMember = activeUser("newcomer");
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.DIRECT)
                .createdBy(actor)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userFinder.findProfileByUserId(actor.getId())).thenReturn(actor);
        when(userFinder.findProfileByUserId(newMember.getId())).thenReturn(newMember);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> conversationMemberService.addMemberToConversation(conversation.getId(), newMember.getId(), actor.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only GROUP conversations support adding members.");
    }

    @Test
    void addMemberToConversationRejectsMissingConversation() {
        User admin = activeUser("admin");
        User newMember = activeUser("newcomer");
        UUID conversationId = UUID.randomUUID();

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(newMember.getId())).thenReturn(newMember);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationMemberService.addMemberToConversation(conversationId, newMember.getId(), admin.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Conversation not found");
    }

    @Test
    void listConversationMembersReturnsPageForActiveMember() {
        User user = activeUser("alice");
        Conversation conversation = groupConversation(user);
        ConversationMember membership = membership(conversation, user, ConversationMemberRole.MEMBER, ConversationMemberStatus.ACTIVE);
        Pageable pageable = PageRequest.of(0, 10);

        when(userFinder.findProfileByUserId(user.getId())).thenReturn(user);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, user)).thenReturn(Optional.of(membership));
        when(conversationMemberRepository.findByConversation(conversation, pageable))
                .thenReturn(new PageImpl<>(List.of(membership), pageable, 1));

        Page<ConversationMemberResponse> response = conversationMemberService.listConversationMembers(conversation.getId(), user.getId(), pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void listConversationMembersRejectsLeftMember() {
        User user = activeUser("alice");
        Conversation conversation = groupConversation(user);
        ConversationMember leftMembership = membership(conversation, user, ConversationMemberRole.MEMBER, ConversationMemberStatus.LEFT);
        Pageable pageable = PageRequest.of(0, 10);

        when(userFinder.findProfileByUserId(user.getId())).thenReturn(user);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, user)).thenReturn(Optional.of(leftMembership));

        assertThatThrownBy(() -> conversationMemberService.listConversationMembers(conversation.getId(), user.getId(), pageable))
                .isInstanceOf(ForbiddenException.class);

        verify(conversationMemberRepository, never()).findByConversation(any(), any());
    }

    @Test
    void listConversationMembersRejectsNonMember() {
        User user = activeUser("stranger");
        Conversation conversation = groupConversation(activeUser("creator"));
        Pageable pageable = PageRequest.of(0, 10);

        when(userFinder.findProfileByUserId(user.getId())).thenReturn(user);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationMemberService.listConversationMembers(conversation.getId(), user.getId(), pageable))
                .isInstanceOf(ForbiddenException.class);

        verify(conversationMemberRepository, never()).findByConversation(any(), any());
    }

    @Test
    void listConversationMembersRejectsMissingConversation() {
        User user = activeUser("alice");
        UUID conversationId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(userFinder.findProfileByUserId(user.getId())).thenReturn(user);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationMemberService.listConversationMembers(conversationId, user.getId(), pageable))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Conversation not found");
    }

    @Test
    void promoteMemberToAdminPromotesActiveMember() {
        User admin = activeUser("admin");
        User member = activeUser("member");
        Conversation conversation = groupConversation(admin);
        ConversationMember adminMembership = membership(conversation, admin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);
        ConversationMember memberMembership = membership(conversation, member, ConversationMemberRole.MEMBER, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(member.getId())).thenReturn(member);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, admin)).thenReturn(Optional.of(adminMembership));
        when(conversationMemberRepository.findByConversationAndUser(conversation, member)).thenReturn(Optional.of(memberMembership));

        conversationMemberService.promoteMemberToAdmin(conversation.getId(), member.getId(), admin.getId());

        assertThat(memberMembership.getRole()).isEqualTo(ConversationMemberRole.ADMIN);
        verify(conversationMemberRepository).save(memberMembership);
    }

    @Test
    void promoteMemberToAdminRejectsWhenAlreadyAdmin() {
        User admin = activeUser("admin");
        User alreadyAdmin = activeUser("otherAdmin");
        Conversation conversation = groupConversation(admin);
        ConversationMember adminMembership = membership(conversation, admin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);
        ConversationMember otherAdminMembership = membership(conversation, alreadyAdmin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(alreadyAdmin.getId())).thenReturn(alreadyAdmin);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, admin)).thenReturn(Optional.of(adminMembership));
        when(conversationMemberRepository.findByConversationAndUser(conversation, alreadyAdmin)).thenReturn(Optional.of(otherAdminMembership));

        assertThatThrownBy(() -> conversationMemberService.promoteMemberToAdmin(conversation.getId(), alreadyAdmin.getId(), admin.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Member is already an admin");
    }

    @Test
    void promoteMemberToAdminRejectsNonAdminActor() {
        User actor = activeUser("actor");
        User member = activeUser("member");
        Conversation conversation = groupConversation(activeUser("creator"));
        ConversationMember actorMembership = membership(conversation, actor, ConversationMemberRole.MEMBER, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(actor.getId())).thenReturn(actor);
        when(userFinder.findProfileByUserId(member.getId())).thenReturn(member);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, actor)).thenReturn(Optional.of(actorMembership));

        assertThatThrownBy(() -> conversationMemberService.promoteMemberToAdmin(conversation.getId(), member.getId(), actor.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User does not have permission to promote members to admin");
    }

    @Test
    void promoteMemberToAdminRejectsInactiveActor() {
        User actor = activeUser("actor");
        User member = activeUser("member");
        Conversation conversation = groupConversation(activeUser("creator"));
        ConversationMember leftActorMembership = membership(conversation, actor, ConversationMemberRole.ADMIN, ConversationMemberStatus.LEFT);

        when(userFinder.findProfileByUserId(actor.getId())).thenReturn(actor);
        when(userFinder.findProfileByUserId(member.getId())).thenReturn(member);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, actor)).thenReturn(Optional.of(leftActorMembership));

        assertThatThrownBy(() -> conversationMemberService.promoteMemberToAdmin(conversation.getId(), member.getId(), actor.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User is not an active member of the conversation");
    }

    @Test
    void promoteMemberToAdminRejectsMissingConversation() {
        User admin = activeUser("admin");
        User member = activeUser("member");
        UUID conversationId = UUID.randomUUID();

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(member.getId())).thenReturn(member);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationMemberService.promoteMemberToAdmin(conversationId, member.getId(), admin.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Conversation not found");
    }

    @Test
    void promoteMemberToAdminRejectsMemberNotFoundInConversation() {
        User admin = activeUser("admin");
        User outsider = activeUser("outsider");
        Conversation conversation = groupConversation(admin);
        ConversationMember adminMembership = membership(conversation, admin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(outsider.getId())).thenReturn(outsider);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, admin)).thenReturn(Optional.of(adminMembership));
        when(conversationMemberRepository.findByConversationAndUser(conversation, outsider)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationMemberService.promoteMemberToAdmin(conversation.getId(), outsider.getId(), admin.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Member is not an active participant of the conversation");
    }

    @Test
    void promoteMemberToAdminRejectsLeftMember() {
        User admin = activeUser("admin");
        User former = activeUser("former");
        Conversation conversation = groupConversation(admin);
        ConversationMember adminMembership = membership(conversation, admin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);
        ConversationMember leftMembership = membership(conversation, former, ConversationMemberRole.MEMBER, ConversationMemberStatus.LEFT);

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(former.getId())).thenReturn(former);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, admin)).thenReturn(Optional.of(adminMembership));
        when(conversationMemberRepository.findByConversationAndUser(conversation, former)).thenReturn(Optional.of(leftMembership));

        assertThatThrownBy(() -> conversationMemberService.promoteMemberToAdmin(conversation.getId(), former.getId(), admin.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Member is not an active participant of the conversation");
    }

    @Test
    void demoteAdminToMemberDemotesActiveAdmin() {
        User admin = activeUser("admin");
        User creator = activeUser("creator");
        User targetAdmin = activeUser("targetAdmin");
        Conversation conversation = groupConversation(creator);
        ConversationMember adminMembership = membership(conversation, admin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);
        ConversationMember targetMembership = membership(conversation, targetAdmin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(targetAdmin.getId())).thenReturn(targetAdmin);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, admin)).thenReturn(Optional.of(adminMembership));
        when(conversationMemberRepository.findByConversationAndUser(conversation, targetAdmin)).thenReturn(Optional.of(targetMembership));

        conversationMemberService.demoteAdminToMember(conversation.getId(), targetAdmin.getId(), admin.getId());

        assertThat(targetMembership.getRole()).isEqualTo(ConversationMemberRole.MEMBER);
        verify(conversationMemberRepository).save(targetMembership);
    }

    @Test
    void demoteAdminToMemberRejectsDemotingCreator() {
        User admin = activeUser("admin");
        User creator = activeUser("creator");
        Conversation conversation = groupConversation(creator);
        ConversationMember adminMembership = membership(conversation, admin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);
        ConversationMember creatorMembership = membership(conversation, creator, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(creator.getId())).thenReturn(creator);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, admin)).thenReturn(Optional.of(adminMembership));
        when(conversationMemberRepository.findByConversationAndUser(conversation, creator)).thenReturn(Optional.of(creatorMembership));

        assertThatThrownBy(() -> conversationMemberService.demoteAdminToMember(conversation.getId(), creator.getId(), admin.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Cannot demote the creator of the conversation");
    }

    @Test
    void demoteAdminToMemberRejectsWhenTargetIsNotAdmin() {
        User admin = activeUser("admin");
        User creator = activeUser("creator");
        User member = activeUser("member");
        Conversation conversation = groupConversation(creator);
        ConversationMember adminMembership = membership(conversation, admin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);
        ConversationMember memberMembership = membership(conversation, member, ConversationMemberRole.MEMBER, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(member.getId())).thenReturn(member);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, admin)).thenReturn(Optional.of(adminMembership));
        when(conversationMemberRepository.findByConversationAndUser(conversation, member)).thenReturn(Optional.of(memberMembership));

        assertThatThrownBy(() -> conversationMemberService.demoteAdminToMember(conversation.getId(), member.getId(), admin.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Member is not an admin");
    }

    @Test
    void demoteAdminToMemberRejectsInactiveActor() {
        User actor = activeUser("actor");
        User target = activeUser("target");
        Conversation conversation = groupConversation(activeUser("creator"));
        ConversationMember leftActorMembership = membership(conversation, actor, ConversationMemberRole.ADMIN, ConversationMemberStatus.LEFT);

        when(userFinder.findProfileByUserId(actor.getId())).thenReturn(actor);
        when(userFinder.findProfileByUserId(target.getId())).thenReturn(target);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, actor)).thenReturn(Optional.of(leftActorMembership));

        assertThatThrownBy(() -> conversationMemberService.demoteAdminToMember(conversation.getId(), target.getId(), actor.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User is not an active member of the conversation");
    }

    @Test
    void demoteAdminToMemberRejectsMissingConversation() {
        User admin = activeUser("admin");
        User target = activeUser("target");
        UUID conversationId = UUID.randomUUID();

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(target.getId())).thenReturn(target);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationMemberService.demoteAdminToMember(conversationId, target.getId(), admin.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Conversation not found");
    }

    @Test
    void demoteAdminToMemberRejectsNonAdminActor() {
        User actor = activeUser("actor");
        User creator = activeUser("creator");
        User targetAdmin = activeUser("targetAdmin");
        Conversation conversation = groupConversation(creator);
        ConversationMember actorMembership = membership(conversation, actor, ConversationMemberRole.MEMBER, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(actor.getId())).thenReturn(actor);
        when(userFinder.findProfileByUserId(targetAdmin.getId())).thenReturn(targetAdmin);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, actor)).thenReturn(Optional.of(actorMembership));

        assertThatThrownBy(() -> conversationMemberService.demoteAdminToMember(conversation.getId(), targetAdmin.getId(), actor.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User does not have permission to demote members from admin");
    }

    @Test
    void removeMemberFromConversationRejectsInactiveActor() {
        User actor = activeUser("actor");
        User member = activeUser("member");
        Conversation conversation = groupConversation(activeUser("creator"));
        ConversationMember leftActorMembership = membership(conversation, actor, ConversationMemberRole.ADMIN, ConversationMemberStatus.LEFT);

        when(userFinder.findProfileByUserId(actor.getId())).thenReturn(actor);
        when(userFinder.findProfileByUserId(member.getId())).thenReturn(member);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, actor)).thenReturn(Optional.of(leftActorMembership));

        assertThatThrownBy(() -> conversationMemberService.removeMemberFromConversation(conversation.getId(), member.getId(), actor.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User is not an active member of the conversation");
    }

    @Test
    void removeMemberFromConversationRejectsMissingConversation() {
        User admin = activeUser("admin");
        User member = activeUser("member");
        UUID conversationId = UUID.randomUUID();

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(member.getId())).thenReturn(member);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationMemberService.removeMemberFromConversation(conversationId, member.getId(), admin.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Conversation not found");
    }

    @Test
    void removeMemberFromConversationRejectsMemberNotFoundInConversation() {
        User admin = activeUser("admin");
        User outsider = activeUser("outsider");
        Conversation conversation = groupConversation(admin);
        ConversationMember adminMembership = membership(conversation, admin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(outsider.getId())).thenReturn(outsider);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, admin)).thenReturn(Optional.of(adminMembership));
        when(conversationMemberRepository.findByConversationAndUser(conversation, outsider)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationMemberService.removeMemberFromConversation(conversation.getId(), outsider.getId(), admin.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Member is not an active participant of the conversation");
    }

    @Test
    void removeMemberFromConversationMarksMemberAsRemovedInsteadOfDeleting() {
        User admin = activeUser("admin");
        User creator = activeUser("creator");
        User member = activeUser("member");
        Conversation conversation = groupConversation(creator);
        ConversationMember adminMembership = membership(conversation, admin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);
        ConversationMember memberMembership = membership(conversation, member, ConversationMemberRole.MEMBER, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(member.getId())).thenReturn(member);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, admin)).thenReturn(Optional.of(adminMembership));
        when(conversationMemberRepository.findByConversationAndUser(conversation, member)).thenReturn(Optional.of(memberMembership));

        conversationMemberService.removeMemberFromConversation(conversation.getId(), member.getId(), admin.getId());

        assertThat(memberMembership.getStatus()).isEqualTo(ConversationMemberStatus.REMOVED);
        assertThat(memberMembership.getLeftAt()).isNotNull();
        verify(conversationMemberRepository).save(memberMembership);
        verify(conversationMemberRepository, never()).delete(any());
    }

    @Test
    void removeMemberFromConversationRejectsRemovingCreator() {
        User admin = activeUser("admin");
        User creator = activeUser("creator");
        Conversation conversation = groupConversation(creator);
        ConversationMember adminMembership = membership(conversation, admin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);
        ConversationMember creatorMembership = membership(conversation, creator, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(creator.getId())).thenReturn(creator);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, admin)).thenReturn(Optional.of(adminMembership));
        when(conversationMemberRepository.findByConversationAndUser(conversation, creator)).thenReturn(Optional.of(creatorMembership));

        assertThatThrownBy(() -> conversationMemberService.removeMemberFromConversation(conversation.getId(), creator.getId(), admin.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Cannot remove the creator of the conversation");
    }

    @Test
    void removeMemberFromConversationRejectsRemovingAnotherAdmin() {
        User admin = activeUser("admin");
        User creator = activeUser("creator");
        User otherAdmin = activeUser("otherAdmin");
        Conversation conversation = groupConversation(creator);
        ConversationMember adminMembership = membership(conversation, admin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);
        ConversationMember otherAdminMembership = membership(conversation, otherAdmin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(otherAdmin.getId())).thenReturn(otherAdmin);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, admin)).thenReturn(Optional.of(adminMembership));
        when(conversationMemberRepository.findByConversationAndUser(conversation, otherAdmin)).thenReturn(Optional.of(otherAdminMembership));

        assertThatThrownBy(() -> conversationMemberService.removeMemberFromConversation(conversation.getId(), otherAdmin.getId(), admin.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Cannot remove an administrator of the conversation");
    }

    @Test
    void removeMemberFromConversationRejectsNonAdminActor() {
        User actor = activeUser("actor");
        User creator = activeUser("creator");
        User member = activeUser("member");
        Conversation conversation = groupConversation(creator);
        ConversationMember actorMembership = membership(conversation, actor, ConversationMemberRole.MEMBER, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(actor.getId())).thenReturn(actor);
        when(userFinder.findProfileByUserId(member.getId())).thenReturn(member);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, actor)).thenReturn(Optional.of(actorMembership));

        assertThatThrownBy(() -> conversationMemberService.removeMemberFromConversation(conversation.getId(), member.getId(), actor.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User does not have permission to remove members from the conversation");
    }

    @Test
    void leaveConversationRejectsMissingConversation() {
        User member = activeUser("member");
        UUID conversationId = UUID.randomUUID();

        when(userFinder.findProfileByUserId(member.getId())).thenReturn(member);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationMemberService.leaveConversation(conversationId, member.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Conversation not found");
    }

    @Test
    void removeMemberFromConversationRejectsAlreadyLeftMember() {
        User admin = activeUser("admin");
        User former = activeUser("former");
        Conversation conversation = groupConversation(admin);
        ConversationMember adminMembership = membership(conversation, admin, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);
        ConversationMember leftMembership = membership(conversation, former, ConversationMemberRole.MEMBER, ConversationMemberStatus.LEFT);

        when(userFinder.findProfileByUserId(admin.getId())).thenReturn(admin);
        when(userFinder.findProfileByUserId(former.getId())).thenReturn(former);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, admin)).thenReturn(Optional.of(adminMembership));
        when(conversationMemberRepository.findByConversationAndUser(conversation, former)).thenReturn(Optional.of(leftMembership));

        assertThatThrownBy(() -> conversationMemberService.removeMemberFromConversation(conversation.getId(), former.getId(), admin.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Member is not an active participant of the conversation");
    }

    @Test
    void leaveConversationMarksActiveMemberAsLeft() {
        User creator = activeUser("creator");
        User member = activeUser("member");
        Conversation conversation = groupConversation(creator);
        ConversationMember memberMembership = membership(conversation, member, ConversationMemberRole.MEMBER, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(member.getId())).thenReturn(member);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, member)).thenReturn(Optional.of(memberMembership));

        conversationMemberService.leaveConversation(conversation.getId(), member.getId());

        assertThat(memberMembership.getStatus()).isEqualTo(ConversationMemberStatus.LEFT);
        assertThat(memberMembership.getLeftAt()).isNotNull();
        verify(conversationMemberRepository).save(memberMembership);
    }

    @Test
    void leaveConversationRejectsCreator() {
        User creator = activeUser("creator");
        Conversation conversation = groupConversation(creator);
        ConversationMember creatorMembership = membership(conversation, creator, ConversationMemberRole.ADMIN, ConversationMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(creator.getId())).thenReturn(creator);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, creator)).thenReturn(Optional.of(creatorMembership));

        assertThatThrownBy(() -> conversationMemberService.leaveConversation(conversation.getId(), creator.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("The creator of the conversation cannot leave it");

        verify(conversationMemberRepository, never()).save(any());
    }

    @Test
    void leaveConversationRejectsWhenMembershipIsAlreadyLeft() {
        User creator = activeUser("creator");
        User member = activeUser("member");
        Conversation conversation = groupConversation(creator);
        ConversationMember leftMembership = membership(conversation, member, ConversationMemberRole.MEMBER, ConversationMemberStatus.LEFT);

        when(userFinder.findProfileByUserId(member.getId())).thenReturn(member);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, member)).thenReturn(Optional.of(leftMembership));

        assertThatThrownBy(() -> conversationMemberService.leaveConversation(conversation.getId(), member.getId()))
                .isInstanceOf(NotFoundException.class);

        verify(conversationMemberRepository, never()).save(any());
    }

    @Test
    void leaveConversationRejectsWhenNotAnActiveMember() {
        User creator = activeUser("creator");
        User outsider = activeUser("outsider");
        Conversation conversation = groupConversation(creator);

        when(userFinder.findProfileByUserId(outsider.getId())).thenReturn(outsider);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, outsider)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationMemberService.leaveConversation(conversation.getId(), outsider.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    private User activeUser(String username) {
        return User.builder().id(UUID.randomUUID()).username(username).status(UserStatus.ACTIVE).build();
    }

    private Conversation groupConversation(User creator) {
        return Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.GROUP)
                .name("Squad")
                .createdBy(creator)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private ConversationMember membership(Conversation conversation, User user, ConversationMemberRole role, ConversationMemberStatus status) {
        return ConversationMember.builder()
                .id(UUID.randomUUID())
                .conversation(conversation)
                .user(user)
                .role(role)
                .status(status)
                .joinedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
