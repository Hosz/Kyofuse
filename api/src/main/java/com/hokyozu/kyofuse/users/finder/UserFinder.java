package com.hokyozu.kyofuse.users.finder;

import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class UserFinder {

    private final UserRepository userRepository;

    public User findProfileByUserId(UUID user) {
        return userRepository.findById(user)
                .orElseThrow(() -> new BadRequestException("User not found for ID: " + user));
    }
}
