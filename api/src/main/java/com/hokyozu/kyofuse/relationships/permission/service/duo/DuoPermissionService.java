package com.hokyozu.kyofuse.relationships.permission.service.duo;

import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendshipRepository;
import com.hokyozu.kyofuse.relationships.shared.validator.BlockValidator;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DuoPermissionService {

    private final BlockValidator blockValidator;
    private final UserFriendshipRepository userFriendshipRepository;

    public void validateSendInvite(User sender, User receiver) {

        blockValidator.validate(sender, receiver);

        if (userFriendshipRepository.existsByUserOneAndUserTwo(sender, receiver)) {
            return;
        }
        if (userFriendshipRepository.existsByUserOneAndUserTwo(receiver, sender)) {
            return;
        }

        //TODO: toda lógica e implementação do sistema de Duo

        throw new ForbiddenException("User does not have permission to send a duo invite.");
    }

    public void validateAcceptInvite(User receiver, User sender) {

        blockValidator.validate(receiver, sender);

        if (userFriendshipRepository.existsByUserOneAndUserTwo(sender, receiver)) {
            return;
        }
        if (userFriendshipRepository.existsByUserOneAndUserTwo(receiver, sender)) {
            return;
        }

        //TODO: toda lógica e implementação do sistema de Duo

        throw new ForbiddenException("User does not have permission to accept a duo invite.");
    }

    public void valideDeclineInvite(User receiver, User sender) {

        //TODO: toda lógica e implementação do sistema de Duo
    }
}
