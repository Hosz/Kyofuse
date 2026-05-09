package com.hokyozu.kyofuse.auth.validator;

import com.hokyozu.kyofuse.shared.exception.ConflictException;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailAndUsernameAvailabilityValidator {

    private final UserRepository userRepository;

    public void validate(String email, String username) {

        if (userRepository.existsByEmailIgnoreCase(email.trim())) {
            throw new ConflictException("Email já está em uso.");
        }

        if (userRepository.existsByUsernameIgnoreCase(username.trim())) {
            throw new ConflictException("Username já está em uso.");
        }
    }
}

