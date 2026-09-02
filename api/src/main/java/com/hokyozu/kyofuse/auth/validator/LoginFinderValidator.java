package com.hokyozu.kyofuse.auth.validator;

import com.hokyozu.kyofuse.auth.dto.request.LoginRequest;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.infrastructure.security.crypto.EmailCipherService;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginFinderValidator {

    private final UserRepository userRepository;
    private final EmailCipherService emailCipherService;

    public User validate(LoginRequest request) {
        String login = request.login().trim().toLowerCase();
        return userRepository.findByEmailIndex(emailCipherService.blindIndex(login))
                .or(() -> userRepository.findByUsernameIgnoreCase(login))
                .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas."));
    }
}
