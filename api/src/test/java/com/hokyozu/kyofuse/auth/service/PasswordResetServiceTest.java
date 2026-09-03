package com.hokyozu.kyofuse.auth.service;

import com.hokyozu.kyofuse.auth.entity.PasswordResetToken;
import com.hokyozu.kyofuse.auth.repository.PasswordResetTokenRepository;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.infrastructure.security.crypto.EmailCipherService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.AccountSwitchSessionRepository;
import com.hokyozu.kyofuse.infrastructure.security.jwt.RefreshTokenRepository;
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimitPolicies;
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimiterService;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AccountSwitchSessionRepository accountSwitchSessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailCipherService emailCipherService;

    @Mock
    private MailService mailService;

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private RateLimitPolicies rateLimitPolicies;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .emailIndex("blind_index_123")
                .username("testuser")
                .passwordHash("old_hashed_password")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void requestPasswordReset_whenUserFoundByEmail_shouldDeleteOldTokensSaveNewAndSendEmail() {
        when(emailCipherService.blindIndex("test@example.com")).thenReturn("blind_index_123");
        when(userRepository.findByEmailIndex("blind_index_123")).thenReturn(Optional.of(user));

        passwordResetService.requestPasswordReset("test@example.com", "127.0.0.1");

        verify(tokenRepository).findByUserId(user.getId());
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken saved = tokenCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(user.getId());
        assertThat(saved.getTokenHash()).isNotBlank();
        verify(mailService).sendPasswordResetEmail(eq("test@example.com"), anyString());
    }

    @Test
    void requestPasswordReset_whenUserNotFound_shouldNotThrowAndNotSendEmail() {
        when(emailCipherService.blindIndex("unknown@example.com")).thenReturn("blind_unknown");
        when(userRepository.findByEmailIndex("blind_unknown")).thenReturn(Optional.empty());

        passwordResetService.requestPasswordReset("unknown@example.com", "127.0.0.1");

        verify(tokenRepository, never()).save(any());
        verifyNoInteractions(mailService);
    }

    @Test
    void validateToken_whenValid_shouldNotThrow() {
        when(tokenRepository.existsById(anyString())).thenReturn(true);

        passwordResetService.validateToken("some_raw_token");
    }

    @Test
    void validateToken_whenTokenNotFound_shouldThrowBadRequestException() {
        when(tokenRepository.existsById(anyString())).thenReturn(false);

        assertThatThrownBy(() -> passwordResetService.validateToken("invalid_raw_token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Token de recuperação inválido ou expirado.");
    }

    @Test
    void resetPassword_whenValid_shouldUpdatePasswordHashAndRevokeSession() {
        PasswordResetToken token = PasswordResetToken.builder()
                .userId(user.getId())
                .tokenHash("some_hash")
                .createdAt(Instant.now())
                .build();

        when(tokenRepository.findById(anyString())).thenReturn(Optional.of(token));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewSecretPassword123!")).thenReturn("new_encoded_hash");

        passwordResetService.resetPassword("raw_token", "NewSecretPassword123!");

        assertThat(user.getPasswordHash()).isEqualTo("new_encoded_hash");
        verify(userRepository).save(user);
        verify(tokenRepository).deleteById(anyString());
        verify(refreshTokenRepository).deleteAllByUser(user);
        verify(accountSwitchSessionRepository).deleteAllByUserId(user.getId());
    }
}
