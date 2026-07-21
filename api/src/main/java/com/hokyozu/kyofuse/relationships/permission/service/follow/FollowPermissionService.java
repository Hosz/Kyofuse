package com.hokyozu.kyofuse.relationships.permission.service.follow;

import com.hokyozu.kyofuse.relationships.follow.enums.FollowStatus;
import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendRequestRepository;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendshipRepository;
import com.hokyozu.kyofuse.relationships.privacy.enums.FollowPermission;
import com.hokyozu.kyofuse.relationships.privacy.repository.UserPrivacySettingsRepository;
import com.hokyozu.kyofuse.relationships.shared.validator.BlockValidator;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowPermissionService {

    private final BlockValidator blockValidator;
    private final UserPrivacySettingsRepository userPrivacySettingsRepository;
    private final UserFollowRepository userFollowRepository;

    public void validateSendFollow(User sender, User receiver) {

        if (sender.equals(receiver)) {
            throw new BadRequestException("Cannot follow yourself.");
        }

        blockValidator.validate(sender, receiver);

        if (userPrivacySettingsRepository.findByUser(receiver).getFollowPermission().equals(FollowPermission.EVERYONE)) {
            return;
        }

        if (existRequest(sender, receiver)) {
            return;
        }

        throw new ForbiddenException("User does not have permission to follow this user.");
    }

    public void validateAcceptFollow(User receiver, User sender) {

        blockValidator.validate(receiver, sender);

        if (existRequest(sender, receiver)) {
            return;
        }

        throw new ForbiddenException("User does not have permission to accept this follow request.");
    }

    public void validateRejectFollow(User receiver, User sender) {

        if (existRequest(receiver, sender)) {
            return;
        }

        throw new ForbiddenException("User does not have permission to reject this follow request.");
    }

    public void validateUnfollow(User sender, User receiver) {

        if (areFollowing(sender, receiver)) {
            return;
        }

        throw new ForbiddenException("User does not have permission to unfollow this user.");
    }

    public void validateRemoveFollower(User owner, User follower) {

        if (beingFollowed(follower, owner)) {
            return;
        }

        throw new ForbiddenException("User does not have permission to remove this follower.");
    }

    private boolean beingFollowed(User follower, User followed) {
        return userFollowRepository.existsByFollowerAndFollowedAndStatus(follower, followed, FollowStatus.ACTIVE) &&
                userFollowRepository.existsByFollowerAndFollowedAndStatus(followed, follower, FollowStatus.ACTIVE);
    }

    private boolean isFollowing(User follower, User followed) {
        return userFollowRepository.existsByFollowerAndFollowedAndStatus(follower, followed, FollowStatus.ACTIVE) &&
                userFollowRepository.existsByFollowerAndFollowedAndStatus(followed, follower, FollowStatus.ACTIVE);
    }

    private boolean existRequest(User receiver, User sender) {
        return userFollowRepository.existsByFollowerAndFollowedAndStatus(sender, receiver, FollowStatus.PENDING)||
                userFollowRepository.existsByFollowerAndFollowedAndStatus(receiver, sender, FollowStatus.PENDING);
    }

    private boolean areFollowing(User follower, User followed) {
        return userFollowRepository.existsByFollowerAndFollowedAndStatus(follower, followed, FollowStatus.ACTIVE);
    }
}
