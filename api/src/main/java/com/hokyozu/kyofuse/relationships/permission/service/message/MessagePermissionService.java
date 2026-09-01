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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessagePermissionService {

    private final BlockValidator blockValidator;
    private final UserFriendshipRepository userFriendshipRepository;
    private final UserPrivacySettingsRepository userPrivacySettingsRepository;
    private final UserFollowRepository userFollowRepository;

    /**
     * Decide se a primeira mensagem de sender pra receiver pode ser enviada e, em caso
     * positivo, se a conversa nasce liberada ou pendente de aprovação do receiver.
     */
    public boolean requiresApprovalForFirstMessage(User sender, User receiver) {
        blockValidator.validate(sender, receiver);

        UserPrivacySettings settings = userPrivacySettingsRepository.findByUser(receiver);
        MessagePermission permission = settings != null ? settings.getMessagePermission() : MessagePermission.EVERYONE;

        if (permission == MessagePermission.NOBODY) {
            throw new ForbiddenException("This user does not accept direct messages.");
        }

        boolean isFriend = areFriends(sender, receiver);
        boolean isFollower = isFollower(sender, receiver);

        if (isFriend) {
            return false;
        }

        if (permission == MessagePermission.FRIENDS) {
            throw new ForbiddenException("This user only accepts messages from friends.");
        }

        if (permission == MessagePermission.FOLLOWERS) {
            if (isFollower) {
                return false;
            }
            throw new ForbiddenException("This user only accepts messages from followers.");
        }

        return !isFollower;
    }

    private boolean areFriends(User viewer, User owner) {
        return userFriendshipRepository.existsByUserOneAndUserTwo(viewer, owner) ||
                userFriendshipRepository.existsByUserOneAndUserTwo(owner, viewer);
    }

    private boolean isFollower(User viewer, User owner) {
        return userFollowRepository.existsByFollowerAndFollowedAndStatus(viewer, owner, FollowStatus.ACTIVE);
    }
}
