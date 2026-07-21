package com.hokyozu.kyofuse.relationships.permission.service.message;

import com.hokyozu.kyofuse.relationships.follow.enums.FollowStatus;
import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendshipRepository;
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

    public void validateSendFirstMessage(User sender, User receiver) {

        blockValidator.validate(sender, receiver);

        if (areFriends(sender, receiver)) {
            return;
        }

        if (userPrivacySettingsRepository.findByUser(receiver).getMessagePermission().equals(MessagePermission.EVERYONE)) {
            return;
        }

        if (isFollower(sender, receiver)) {
            return;
        }

        //TODO: receiver deve aceitar mensagens; caso seja a primeira mensagem, cria solicitação;

        throw new ForbiddenException("You do not have permission to send a message to this user.");
    }

    public void validateContinueConversation(User sender, User receiver) {

        blockValidator.validate(sender, receiver);

        if (areFriends(sender, receiver)) {
            return;
        }

        if (isFollower(sender, receiver)) {
            return;
        }

        //TODO falta: seguidores devem possuir autorização

        throw new ForbiddenException("You do not have permission to continue the conversation with this user.");
    }

    public void validateRevokeConversationPermission(User receiver, User sender) {
        //TODO: deve existir autorização ativa
    }

    private boolean areFriends(User viewer, User owner) {
        return userFriendshipRepository.existsByUserOneAndUserTwo(viewer, owner);
    }

    private boolean isFollower(User viewer, User owner) {
        return userFollowRepository.existsByFollowerAndFollowedAndStatus(viewer, owner, FollowStatus.ACTIVE);
    }

}
