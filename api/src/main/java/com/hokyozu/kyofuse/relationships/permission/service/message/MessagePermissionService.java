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

        boolean isMutual = hasMutualRelationship(sender, receiver);
        if (isMutual) {
            return false;
        }

        UserPrivacySettings settings = userPrivacySettingsRepository.findByUser(receiver);
        MessagePermission permission = settings != null ? settings.getMessagePermission() : MessagePermission.EVERYONE;

        if (permission == MessagePermission.NOBODY) {
            throw new ForbiddenException("This user does not accept direct messages.");
        }

        return true;
    }

    private boolean hasMutualRelationship(User userA, User userB) {
        return areFriends(userA, userB) || areMutualFollowers(userA, userB);
    }

    private boolean areFriends(User viewer, User owner) {
        return userFriendshipRepository.existsMutualFriendship(viewer, owner);
    }

    private boolean areMutualFollowers(User viewer, User owner) {
        return userFollowRepository.existsByFollowerAndFollowedAndStatus(viewer, owner, FollowStatus.ACTIVE) &&
                userFollowRepository.existsByFollowerAndFollowedAndStatus(owner, viewer, FollowStatus.ACTIVE);
    }
}
