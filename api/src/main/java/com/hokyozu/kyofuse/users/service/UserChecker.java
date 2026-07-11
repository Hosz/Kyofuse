package com.hokyozu.kyofuse.users.service;

import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class UserChecker {
    public void checkActive(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Usuário não ativo.");
        }
    }
}
