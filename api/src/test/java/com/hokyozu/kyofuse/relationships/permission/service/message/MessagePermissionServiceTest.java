package com.hokyozu.kyofuse.relationships.permission.service.message;

import com.hokyozu.kyofuse.relationships.follow.enums.FollowStatus;
import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendshipRepository;
import com.hokyozu.kyofuse.relationships.privacy.entity.UserPrivacySettings;
import com.hokyozu.kyofuse.relationships.privacy.enums.MessagePermission;
import com.hokyozu.kyofuse.relationships.privacy.repository.UserPrivacySettingsRepository;
import com.hokyozu.kyofuse.relationships.shared.validator.BlockValidator;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessagePermissionServiceTest {

    @Mock
    private BlockValidator blockValidator;

    @Mock
    private UserFriendshipRepository userFriendshipRepository;

    @Mock
    private UserPrivacySettingsRepository userPrivacySettingsRepository;

    @Mock
    private UserFollowRepository userFollowRepository;

    @InjectMocks
    private MessagePermissionService messagePermissionService;

    @Test
    void requiresApprovalPropagatesBlockException() {
        User sender = user("alice");
        User receiver = user("bob");
        doThrow(new ForbiddenException("User is blocked by the profile owner."))
                .when(blockValidator).validate(sender, receiver);

        assertThatThrownBy(() -> messagePermissionService.requiresApprovalForFirstMessage(sender, receiver))
                .isInstanceOf(ForbiddenException.class);

        verify(userFriendshipRepository, never()).existsByUserOneAndUserTwo(sender, receiver);
    }

    @Test
    void requiresApprovalReturnsFalseForFriends() {
        User sender = user("alice");
        User receiver = user("bob");
        when(userFriendshipRepository.existsByUserOneAndUserTwo(sender, receiver)).thenReturn(true);

        boolean result = messagePermissionService.requiresApprovalForFirstMessage(sender, receiver);

        assertThat(result).isFalse();
        verify(userPrivacySettingsRepository, never()).findByUser(receiver);
    }

    @Test
    void requiresApprovalReturnsFalseForActiveFollower() {
        User sender = user("alice");
        User receiver = user("bob");
        when(userFriendshipRepository.existsByUserOneAndUserTwo(sender, receiver)).thenReturn(false);
        when(userFollowRepository.existsByFollowerAndFollowedAndStatus(sender, receiver, FollowStatus.ACTIVE)).thenReturn(true);

        boolean result = messagePermissionService.requiresApprovalForFirstMessage(sender, receiver);

        assertThat(result).isFalse();
        verify(userPrivacySettingsRepository, never()).findByUser(receiver);
    }

    @Test
    void requiresApprovalReturnsTrueForStrangerWhenPermissionIsEveryone() {
        User sender = user("alice");
        User receiver = user("bob");
        when(userFriendshipRepository.existsByUserOneAndUserTwo(sender, receiver)).thenReturn(false);
        when(userFollowRepository.existsByFollowerAndFollowedAndStatus(sender, receiver, FollowStatus.ACTIVE)).thenReturn(false);
        when(userPrivacySettingsRepository.findByUser(receiver)).thenReturn(privacySettings(MessagePermission.EVERYONE));

        boolean result = messagePermissionService.requiresApprovalForFirstMessage(sender, receiver);

        assertThat(result).isTrue();
    }

    @Test
    void requiresApprovalRejectsStrangerWhenPermissionIsFriends() {
        User sender = user("alice");
        User receiver = user("bob");
        when(userFriendshipRepository.existsByUserOneAndUserTwo(sender, receiver)).thenReturn(false);
        when(userFollowRepository.existsByFollowerAndFollowedAndStatus(sender, receiver, FollowStatus.ACTIVE)).thenReturn(false);
        when(userPrivacySettingsRepository.findByUser(receiver)).thenReturn(privacySettings(MessagePermission.FRIENDS));

        assertThatThrownBy(() -> messagePermissionService.requiresApprovalForFirstMessage(sender, receiver))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You do not have permission to send a message to this user.");
    }

    @Test
    void requiresApprovalRejectsNonFollowerWhenPermissionIsFollowers() {
        User sender = user("alice");
        User receiver = user("bob");
        when(userFriendshipRepository.existsByUserOneAndUserTwo(sender, receiver)).thenReturn(false);
        when(userFollowRepository.existsByFollowerAndFollowedAndStatus(sender, receiver, FollowStatus.ACTIVE)).thenReturn(false);
        when(userPrivacySettingsRepository.findByUser(receiver)).thenReturn(privacySettings(MessagePermission.FOLLOWERS));

        assertThatThrownBy(() -> messagePermissionService.requiresApprovalForFirstMessage(sender, receiver))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void requiresApprovalRejectsStrangerWhenPermissionIsNobody() {
        User sender = user("alice");
        User receiver = user("bob");
        when(userFriendshipRepository.existsByUserOneAndUserTwo(sender, receiver)).thenReturn(false);
        when(userFollowRepository.existsByFollowerAndFollowedAndStatus(sender, receiver, FollowStatus.ACTIVE)).thenReturn(false);
        when(userPrivacySettingsRepository.findByUser(receiver)).thenReturn(privacySettings(MessagePermission.NOBODY));

        assertThatThrownBy(() -> messagePermissionService.requiresApprovalForFirstMessage(sender, receiver))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void validateContinueConversationAllowsFriends() {
        User sender = user("alice");
        User receiver = user("bob");
        when(userFriendshipRepository.existsByUserOneAndUserTwo(sender, receiver)).thenReturn(true);

        messagePermissionService.validateContinueConversation(sender, receiver);

        verify(userFollowRepository, never()).existsByFollowerAndFollowedAndStatus(sender, receiver, FollowStatus.ACTIVE);
    }

    @Test
    void validateContinueConversationAllowsActiveFollower() {
        User sender = user("alice");
        User receiver = user("bob");
        when(userFriendshipRepository.existsByUserOneAndUserTwo(sender, receiver)).thenReturn(false);
        when(userFollowRepository.existsByFollowerAndFollowedAndStatus(sender, receiver, FollowStatus.ACTIVE)).thenReturn(true);

        messagePermissionService.validateContinueConversation(sender, receiver);
    }

    @Test
    void validateContinueConversationRejectsStranger() {
        User sender = user("alice");
        User receiver = user("bob");
        when(userFriendshipRepository.existsByUserOneAndUserTwo(sender, receiver)).thenReturn(false);
        when(userFollowRepository.existsByFollowerAndFollowedAndStatus(sender, receiver, FollowStatus.ACTIVE)).thenReturn(false);

        assertThatThrownBy(() -> messagePermissionService.validateContinueConversation(sender, receiver))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You do not have permission to continue the conversation with this user.");
    }

    @Test
    void validateContinueConversationPropagatesBlockException() {
        User sender = user("alice");
        User receiver = user("bob");
        doThrow(new ForbiddenException("User is blocked by the profile owner."))
                .when(blockValidator).validate(sender, receiver);

        assertThatThrownBy(() -> messagePermissionService.validateContinueConversation(sender, receiver))
                .isInstanceOf(ForbiddenException.class);
    }

    private User user(String username) {
        return User.builder().id(UUID.randomUUID()).username(username).status(UserStatus.ACTIVE).build();
    }

    private UserPrivacySettings privacySettings(MessagePermission permission) {
        return UserPrivacySettings.builder().messagePermission(permission).build();
    }
}
