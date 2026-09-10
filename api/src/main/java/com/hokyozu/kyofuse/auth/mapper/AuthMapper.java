package com.hokyozu.kyofuse.auth.mapper;

import com.hokyozu.kyofuse.auth.dto.request.RegisterRequest;
import com.hokyozu.kyofuse.auth.dto.response.AuthResponse;
import com.hokyozu.kyofuse.auth.dto.response.RegisterResponse;
import com.hokyozu.kyofuse.auth.dto.response.SwitchAccountResponse;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;

import java.time.Instant;

public class AuthMapper {

    private AuthMapper() {}

    public static User toEntity(RegisterRequest request, String passwordHash, String emailIndex) {
        Instant now = Instant.now();
        return User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(request.email().trim())
                .emailIndex(emailIndex)
                .username(request.username().trim())
                .passwordHash(passwordHash)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static User toSteamEntity(String steamId, String firstName, String syntheticEmail, String emailIndex, String username, String passwordHash) {
        Instant now = Instant.now();
        return User.builder()
                .steamId(steamId)
                .firstName(firstName.trim())
                .lastName("Steam")
                .email(syntheticEmail)
                .emailIndex(emailIndex)
                .username(username)
                .passwordHash(passwordHash)
                .hasCustomPassword(false)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static User toGoogleEntity(String googleId, String givenName, String familyName, String email, String emailIndex, String username, String passwordHash) {
        Instant now = Instant.now();
        return User.builder()
                .googleId(googleId)
                .firstName(givenName.trim())
                .lastName(familyName.trim())
                .email(email.trim())
                .emailIndex(emailIndex)
                .username(username)
                .passwordHash(passwordHash)
                .hasCustomPassword(false)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .emailVerifiedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static AuthResponse toResponse(User user) {
        return toResponse(user, null, false);
    }

    public static AuthResponse toResponse(User user, String switchToken) {
        return toResponse(user, switchToken, false);
    }

    public static AuthResponse toResponse(User user, String switchToken, boolean deviceTrusted) {
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name(),
                switchToken,
                deviceTrusted
        );
    }

    public static SwitchAccountResponse toSwitchResponse(User user, String switchToken) {
        return new SwitchAccountResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name(),
                switchToken
        );
    }

    public static RegisterResponse toRegisterResponse(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.isEmailVerified(),
                "Cadastro realizado com sucesso! Enviamos um link de confirmação para o seu e-mail."
        );
    }
}
