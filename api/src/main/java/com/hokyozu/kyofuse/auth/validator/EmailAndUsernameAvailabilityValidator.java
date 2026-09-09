package com.hokyozu.kyofuse.auth.validator;

import com.hokyozu.kyofuse.shared.exception.ConflictException;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailAndUsernameAvailabilityValidator {

    private final UserRepository userRepository;

    public void validate(String emailIndex, String username) {

        if (userRepository.existsByEmailIndexAndEmailVerifiedTrue(emailIndex)) {
            throw new ConflictException("Email já está em uso.");
        }

        if (userRepository.existsByUsernameIgnoreCaseAndEmailVerifiedTrue(username.trim())) {
            throw new ConflictException("Username já está em uso.");
        }
    }
}

