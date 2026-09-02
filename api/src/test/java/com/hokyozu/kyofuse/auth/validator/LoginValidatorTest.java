package com.hokyozu.kyofuse.auth.validator;

import com.hokyozu.kyofuse.auth.dto.request.LoginRequest;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginValidatorTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginValidator validator;

    @Test
    void validatePassesWhenPasswordMatchesAndUserIsActiveAndVerified() {
        User user = User.builder()
                .passwordHash("hash")
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        LoginRequest request = new LoginRequest("player", "password123");
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);

        assertThatCode(() -> validator.validate(user, request)).doesNotThrowAnyException();
    }

    @Test
    void validateThrowsWhenPasswordDoesNotMatch() {
        User user = User.builder()
                .passwordHash("hash")
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        LoginRequest request = new LoginRequest("player", "wrong-password");
        when(passwordEncoder.matches("wrong-password", "hash")).thenReturn(false);

        assertThatThrownBy(() -> validator.validate(user, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciais inválidas.");
    }

    @Test
    void validateThrowsWhenEmailIsNotVerified() {
        User user = User.builder()
                .passwordHash("hash")
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();
        LoginRequest request = new LoginRequest("player", "password123");
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);

        assertThatThrownBy(() -> validator.validate(user, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("E-mail não verificado. Verifique seu e-mail para ativar sua conta.");
    }

    @Test
    void validateThrowsWhenUserIsBanned() {
        User user = User.builder()
                .passwordHash("hash")
                .status(UserStatus.BANNED)
                .emailVerified(true)
                .build();
        LoginRequest request = new LoginRequest("player", "password123");
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);

        assertThatThrownBy(() -> validator.validate(user, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Sua conta foi suspensa.");
    }
}
