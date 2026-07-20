package com.hokyozu.kyofuse.relationships.permission.service.profile;

import com.hokyozu.kyofuse.relationships.block.repository.UserBlockRepository;
import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendshipRepository;
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

    private final UserBlockRepository userBlockRepository;
    private final UserPrivacySettingsRepository userPrivacySettingsRepository;
    private final UserFriendshipRepository userFriendshipRepository;
    private final UserFollowRepository userFollowRepository;
    private final BlockValidator blockValidator;

    public void validateViewProfile(User viewer, User owner) {

        validatePrivateProfileAccess(viewer, owner);

        throw new ForbiddenException("User does not have permission to view this profile.");
    }

    public void validateViewPosts(User viewer, User owner) {

        validatePrivateProfileAccess(viewer, owner);

        throw new ForbiddenException("User does not have permission to view this user's posts.");
    }

    public void validateViewFollowers(User viewer, User owner) {

        validatePrivateProfileAccess(viewer, owner);

        if (userPrivacySettingsRepository.findByUser(owner).getFollowersVisibility().equals(ProfileVisibility.PUBLIC)) {
            return;
        }

        throw new ForbiddenException("User does not have permission to view this user's followers.");
    }

    public void validateViewFollowing(User viewer, User owner) {

        validatePrivateProfileAccess(viewer, owner);

        if (userPrivacySettingsRepository.findByUser(owner).getFollowingVisibility().equals(ProfileVisibility.PUBLIC)) {
            return;
        }

        throw new ForbiddenException("User does not have permission to view this user's following.");
    }

    public void validateViewFriends(User viewer, User owner) {

        validatePrivateProfileAccess(viewer, owner);

        throw new ForbiddenException("User does not have permission to view this user's friends.");
    }

    private boolean isFollower(User viewer, User owner) {
        return userFollowRepository.existsByFollowerAndFollowed(viewer, owner);
    }

    private boolean areFriends(User viewer, User owner) {
        return userFriendshipRepository.existsByUserOneAndUserTwo(viewer, owner);
    }

    private boolean isPrivate(User owner) {
        return userPrivacySettingsRepository.findByUser(owner).getProfileVisibility().equals(ProfileVisibility.PRIVATE);
    }

    private void validatePrivateProfileAccess(User viewer, User owner) {
        blockValidator.validate(viewer, owner);

        if (!isPrivate(owner)) {
            return;
        }

        if (areFriends(viewer, owner)) {
            return;
        }

        if (isFollower(viewer, owner)) {
            return;
        }
    }
}
