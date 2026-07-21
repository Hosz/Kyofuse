package com.hokyozu.kyofuse.relationships.permission.service.friendship;

import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendRequestRepository;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendshipRepository;
import com.hokyozu.kyofuse.relationships.privacy.enums.FriendRequestPermission;
import com.hokyozu.kyofuse.relationships.privacy.repository.UserPrivacySettingsRepository;
import com.hokyozu.kyofuse.relationships.shared.validator.BlockValidator;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendshipPermissionService {

    private final BlockValidator blockValidator;
    private final UserFriendshipRepository userFriendshipRepository;
    private final UserFriendRequestRepository userFriendRequestRepository;
    private final UserPrivacySettingsRepository userPrivacySettingsRepository;

    public void validateSendFriendRequest(User sender, User receiver) {

        if (sender.equals(receiver)) {
            throw new BadRequestException("Cannot send friend request to yourself.");
        }

        blockValidator.validate(sender, receiver);

        if (areFriends(sender, receiver)) {
            throw new BadRequestException("Users are already friends.");
        }

        if (!haveFriendRequest(sender, receiver)) {
            return;
        }

        if (userPrivacySettingsRepository.findByUser(receiver).getFriendRequestPermission().equals(FriendRequestPermission.EVERYONE)) {
            return;
        }

        throw new ForbiddenException("Cannot send friend request to this user.");
    }

    public void validateAcceptFriendRequest(User receiver, User sender) {

        if (haveFriendRequest(sender, receiver)) {
            return;
        }

        if (areFriends(sender, receiver)) {
            return;
        }

        blockValidator.validate(receiver, sender);

        throw new ForbiddenException("Cannot accept friend request from this user.");
    }

    public void validateRejectFriendRequest(User receiver, User sender) {

        if (haveFriendRequest(sender, receiver)) {
            return;
        }

        throw new ForbiddenException("Cannot reject friend request from this user.");
    }

    public void validateCancelFriendRequest(User sender, User receiver) {

        if (haveFriendRequest(sender, receiver)) {
            return;
        }

        throw new ForbiddenException("Cannot cancel friend request from this user.");
    }

    public void validateRemoveFriendship(User userOne, User userTwo) {

        if (areFriends(userOne, userTwo)) {
            return;
        }

        throw new ForbiddenException("Cannot remove friendship with this user.");
    }

    private boolean areFriends(User userOne, User userTwo) {
        return userFriendshipRepository.existsByUserOneAndUserTwo(userOne, userTwo) ||
                userFriendshipRepository.existsByUserOneAndUserTwo(userTwo, userOne);
    }

    private boolean haveFriendRequest(User sender, User receiver) {
        return userFriendRequestRepository.existsBySenderAndReceiver(sender, receiver) ||
                userFriendRequestRepository.existsBySenderAndReceiver(receiver, sender);
    }

}
