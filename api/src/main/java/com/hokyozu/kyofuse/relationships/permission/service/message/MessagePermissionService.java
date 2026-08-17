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

    /**
     * Decide se a primeira mensagem de sender pra receiver pode ser enviada e, em caso
     * positivo, se a conversa nasce liberada ou pendente de aprovação do receiver.
     * <p>
     * message_permission só controla QUEM pode tentar mandar a primeira mensagem
     * (o "portão" de entrada); quem decide se a conversa nasce ACCEPTED ou PENDING é
     * exclusivamente a relação entre os dois (amizade ou follow) — inclusive com
     * message_permission = EVERYONE, um estranho ainda passa por aprovação.
     *
     * @return true se a conversa deve nascer PENDING (precisa de aprovação do receiver),
     *         false se pode nascer ACCEPTED direto.
     * @throws ForbiddenException se o envio nem for permitido (bloqueio, ou
     *         message_permission não permite esse sender: FRIENDS sem amizade,
     *         FOLLOWERS sem follow, ou NOBODY).
     */
    public boolean requiresApprovalForFirstMessage(User sender, User receiver) {

        blockValidator.validate(sender, receiver);

        if (areFriends(sender, receiver)) {
            return false;
        }

        if (isFollower(sender, receiver)) {
            return false;
        }

        if (userPrivacySettingsRepository.findByUser(receiver).getMessagePermission().equals(MessagePermission.EVERYONE)) {
            return true;
        }

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

    private boolean areFriends(User viewer, User owner) {
        return userFriendshipRepository.existsByUserOneAndUserTwo(viewer, owner);
    }

    private boolean isFollower(User viewer, User owner) {
        return userFollowRepository.existsByFollowerAndFollowedAndStatus(viewer, owner, FollowStatus.ACTIVE);
    }

}
