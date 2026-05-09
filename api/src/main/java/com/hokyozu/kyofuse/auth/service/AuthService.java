package com.hokyozu.kyofuse.auth.service;

import com.hokyozu.kyofuse.auth.dto.request.LoginRequest;
import com.hokyozu.kyofuse.auth.dto.request.RegisterRequest;
import com.hokyozu.kyofuse.auth.dto.response.AuthResponse;
import com.hokyozu.kyofuse.auth.mapper.AuthMapper;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.auth.validator.EmailAndUsernameAvailabilityValidator;
import com.hokyozu.kyofuse.auth.validator.LoginFinderValidator;
import com.hokyozu.kyofuse.auth.validator.LoginValidator;
import com.hokyozu.kyofuse.infrastructure.security.jwt.JwtService;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final EmailAndUsernameAvailabilityValidator emailAndUsernameAvailabilityValidator;
    private final LoginFinderValidator loginFinderValidator;
    private final LoginValidator loginValidator;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

       emailAndUsernameAvailabilityValidator.validate(request.email(), request.username());
       String passwordHash = passwordEncoder.encode(request.password());
       User user = AuthMapper.toEntity(request, passwordHash);
       User savedUser = userRepository.save(user);

       String token = jwtService.generateToken(savedUser);

       return AuthMapper.toResponse(savedUser, token);
    }

    public AuthResponse login(LoginRequest request) {

        User user = loginFinderValidator.validate(request);
        loginValidator.validate(user, request);

        String token = jwtService.generateToken(user);

        return AuthMapper.toResponse(user, token);
    }
}
