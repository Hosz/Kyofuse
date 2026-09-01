package com.hokyozu.kyofuse.relationships.permission.service.profile;

import com.hokyozu.kyofuse.relationships.follow.enums.FollowStatus;
import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendshipRepository;
import com.hokyozu.kyofuse.relationships.privacy.entity.UserPrivacySettings;
import com.hokyozu.kyofuse.relationships.privacy.enums.ProfileVisibility;
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfilePermissionServiceTest {

    @Mock
    private UserPrivacySettingsRepository userPrivacySettingsRepository;

    @Mock
    private UserFriendshipRepository userFriendshipRepository;

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private BlockValidator blockValidator;

    @InjectMocks
    private ProfilePermissionService profilePermissionService;

    @Test
    void validateViewProfileAllowsPrivateProfileToStrangers() {
        User viewer = user("viewer");
        User owner = user("owner");

        assertThatCode(() -> profilePermissionService.validateViewProfile(viewer, owner))
                .doesNotThrowAnyException();
    }

    @Test
    void validateViewProfileStillRejectsWhenThereIsABlock() {
        User viewer = user("viewer");
        User owner = user("owner");
        doThrow(new ForbiddenException("User is blocked by the profile owner."))
                .when(blockValidator).validate(viewer, owner);

        assertThatThrownBy(() -> profilePermissionService.validateViewProfile(viewer, owner))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void validateViewPostsRejectsStrangersOnPrivateProfile() {
        User viewer = user("viewer");
        User owner = user("owner");
        when(userPrivacySettingsRepository.findByUser(owner)).thenReturn(settings(ProfileVisibility.PRIVATE));
        when(userFriendshipRepository.existsByUserOneAndUserTwo(viewer, owner)).thenReturn(false);
        when(userFollowRepository.existsByFollowerAndFollowedAndStatus(viewer, owner, FollowStatus.ACTIVE)).thenReturn(false);

        assertThatThrownBy(() -> profilePermissionService.validateViewPosts(viewer, owner))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User does not have permission to view this user's posts.");
    }

    @Test
    void validateViewPostsAllowsFollowerOnPrivateProfile() {
        User viewer = user("viewer");
        User owner = user("owner");
        when(userPrivacySettingsRepository.findByUser(owner)).thenReturn(settings(ProfileVisibility.PRIVATE));
        when(userFollowRepository.existsByFollowerAndFollowedAndStatus(viewer, owner, FollowStatus.ACTIVE)).thenReturn(true);

        assertThatCode(() -> profilePermissionService.validateViewPosts(viewer, owner))
                .doesNotThrowAnyException();
    }

    @Test
    void validateViewPostsAllowsAnyoneOnPublicProfile() {
        User viewer = user("viewer");
        User owner = user("owner");
        when(userPrivacySettingsRepository.findByUser(owner)).thenReturn(settings(ProfileVisibility.PUBLIC));

        assertThatCode(() -> profilePermissionService.validateViewPosts(viewer, owner))
                .doesNotThrowAnyException();
    }

    @Test
    void validateViewFollowersRejectsStrangersOnPrivateProfile() {
        User viewer = user("viewer");
        User owner = user("owner");
        when(userPrivacySettingsRepository.findByUser(owner)).thenReturn(settings(ProfileVisibility.PRIVATE));
        when(userFriendshipRepository.existsByUserOneAndUserTwo(viewer, owner)).thenReturn(false);
        when(userFollowRepository.existsByFollowerAndFollowedAndStatus(viewer, owner, FollowStatus.ACTIVE)).thenReturn(false);

        assertThatThrownBy(() -> profilePermissionService.validateViewFollowers(viewer, owner))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User does not have permission to view this user's followers.");
    }

    @Test
    void validateViewFriendsRejectsStrangersOnPrivateProfile() {
        User viewer = user("viewer");
        User owner = user("owner");
        when(userPrivacySettingsRepository.findByUser(owner)).thenReturn(settings(ProfileVisibility.PRIVATE));
        when(userFriendshipRepository.existsByUserOneAndUserTwo(viewer, owner)).thenReturn(false);
        when(userFollowRepository.existsByFollowerAndFollowedAndStatus(viewer, owner, FollowStatus.ACTIVE)).thenReturn(false);

        assertThatThrownBy(() -> profilePermissionService.validateViewFriends(viewer, owner))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User does not have permission to view this user's friends.");
    }

    @Test
    void validateViewFollowingRejectsStrangersOnPrivateProfile() {
        User viewer = user("viewer");
        User owner = user("owner");
        when(userPrivacySettingsRepository.findByUser(owner)).thenReturn(settings(ProfileVisibility.PRIVATE));
        when(userFriendshipRepository.existsByUserOneAndUserTwo(viewer, owner)).thenReturn(false);
        when(userFollowRepository.existsByFollowerAndFollowedAndStatus(viewer, owner, FollowStatus.ACTIVE)).thenReturn(false);

        assertThatThrownBy(() -> profilePermissionService.validateViewFollowing(viewer, owner))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User does not have permission to view this user's following.");
    }

    private User user(String username) {
        return User.builder().id(UUID.randomUUID()).username(username).status(UserStatus.ACTIVE).build();
    }

    private UserPrivacySettings settings(ProfileVisibility profileVisibility) {
        return UserPrivacySettings.builder()
                .profileVisibility(profileVisibility)
                .build();
    }
}
