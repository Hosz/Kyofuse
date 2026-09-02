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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfilePermissionService {

    private final UserPrivacySettingsRepository userPrivacySettingsRepository;
    private final UserFriendshipRepository userFriendshipRepository;
    private final UserFollowRepository userFollowRepository;
    private final BlockValidator blockValidator;

    public void validateViewProfile(User viewer, User owner) {
        blockValidator.validate(viewer, owner);
    }

    public void validateViewPosts(User viewer, User owner) {
        try {
            validatePrivateProfileAccess(viewer, owner);
        } catch (ForbiddenException e) {
            throw new ForbiddenException("User does not have permission to view this user's posts.");
        }
    }

    public void validateViewFollowers(User viewer, User owner) {
        try {
            validatePrivateProfileAccess(viewer, owner);
        } catch (ForbiddenException e) {
            throw new ForbiddenException("User does not have permission to view this user's followers.");
        }
    }

    public void validateViewFollowing(User viewer, User owner) {
        try {
            validatePrivateProfileAccess(viewer, owner);
        } catch (ForbiddenException e) {
            throw new ForbiddenException("User does not have permission to view this user's following.");
        }
    }

    public void validateViewFriends(User viewer, User owner) {
        try {
            validatePrivateProfileAccess(viewer, owner);
        } catch (ForbiddenException e) {
            throw new ForbiddenException("User does not have permission to view this user's friends.");
        }
    }

    private boolean isFollower(User viewer, User owner) {
        return userFollowRepository.existsByFollowerAndFollowedAndStatus(viewer, owner, FollowStatus.ACTIVE);
    }

    private boolean areFriends(User viewer, User owner) {
        return userFriendshipRepository.existsByUserOneAndUserTwo(viewer, owner) ||
                userFriendshipRepository.existsByUserOneAndUserTwo(owner, viewer);
    }

    private void validatePrivateProfileAccess(User viewer, User owner) {
        blockValidator.validate(viewer, owner);

        if (viewer.getId().equals(owner.getId())) {
            return;
        }

        UserPrivacySettings settings = userPrivacySettingsRepository.findByUser(owner);
        ProfileVisibility visibility = settings != null ? settings.getProfileVisibility() : ProfileVisibility.PUBLIC;

        switch (visibility) {
            case PUBLIC -> {}
            case FOLLOWERS, PRIVATE -> {
                if (!isFollower(viewer, owner) && !areFriends(viewer, owner)) {
                    throw new ForbiddenException("User does not have permission to view this profile.");
                }
            }
            case FRIENDS -> {
                if (!areFriends(viewer, owner)) {
                    throw new ForbiddenException("User does not have permission to view this profile.");
                }
            }
        }
    }
}
