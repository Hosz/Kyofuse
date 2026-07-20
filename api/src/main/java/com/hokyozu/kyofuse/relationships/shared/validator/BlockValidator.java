package com.hokyozu.kyofuse.relationships.shared.validator;

import com.hokyozu.kyofuse.relationships.block.repository.UserBlockRepository;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlockValidator {

    private final UserBlockRepository userBlockRepository;

    public void validate(User viewer, User owner) {
        if (userBlockRepository.existsByBlockerAndBlocked(owner, viewer)) {
            throw new ForbiddenException("User is blocked by the profile owner.");
        }
        if (userBlockRepository.existsByBlockerAndBlocked(viewer, owner)) {
            throw new ForbiddenException("User has blocked the profile owner.");
        }
    }
}
