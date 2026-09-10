package com.hokyozu.kyofuse.auth.service;

import com.hokyozu.kyofuse.auth.entity.EmailVerificationToken;
import com.hokyozu.kyofuse.auth.repository.EmailVerificationTokenRepository;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.infrastructure.security.crypto.EmailCipherService;
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimitPolicies;
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimiterService;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private EmailCipherService emailCipherService;

    @Mock
    private MailService mailService;

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private RateLimitPolicies rateLimitPolicies;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .emailIndex("email_idx_123")
                .emailVerified(false)
                .build();
    }

    private String hashToken(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createVerificationTokenSavesTokenAndSendsEmail() {
        emailVerificationService.createVerificationToken(user);

        verify(tokenRepository).deleteAllByUser(user);
        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(captor.capture());
        EmailVerificationToken saved = captor.getValue();

        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getTokenType()).isEqualTo("REGISTRATION");
        assertThat(saved.getTokenHash()).isNotBlank();
        verify(mailService).sendEmailVerificationEmail(eq("test@example.com"), any());
    }

    @Test
    void createLinkVerificationTokenSavesTokenAndSendsEmail() {
        emailVerificationService.createLinkVerificationToken(user, "new@example.com", "new_idx_456");

        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(captor.capture());
        EmailVerificationToken saved = captor.getValue();

        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getTokenType()).isEqualTo("LINK_EMAIL");
        assertThat(saved.getPendingEmail()).isEqualTo("new@example.com");
        assertThat(saved.getPendingEmailIndex()).isEqualTo("new_idx_456");
        verify(mailService).sendEmailLinkVerificationEmail(eq("new@example.com"), any());
    }

    @Test
    void verifyEmailRegistrationSuccess() {
        String rawToken = "my-raw-token";
        String tokenHash = hashToken(rawToken);

        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .tokenType("REGISTRATION")
                .expiresAt(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .build();

        when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(token));
        when(userRepository.existsByEmailIndexAndEmailVerifiedTrueAndIdNot("email_idx_123", userId)).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCaseAndEmailVerifiedTrueAndIdNot("testuser", userId)).thenReturn(false);

        User verified = emailVerificationService.verifyEmail(rawToken);

        assertThat(verified.isEmailVerified()).isTrue();
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).deleteAllByPendingEmailIndex("email_idx_123");
    }

    @Test
    void verifyEmailRegistrationThrowsWhenEmailAlreadyVerifiedByAnotherUser() {
        String rawToken = "my-raw-token";
        String tokenHash = hashToken(rawToken);

        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .tokenType("REGISTRATION")
                .expiresAt(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .build();

        when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(token));
        when(userRepository.existsByEmailIndexAndEmailVerifiedTrueAndIdNot("email_idx_123", userId)).thenReturn(true);

        assertThatThrownBy(() -> emailVerificationService.verifyEmail(rawToken))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("já foi verificado");

        assertThat(user.isEmailVerified()).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyEmailLinkSuccess() {
        String rawToken = "my-link-token";
        String tokenHash = hashToken(rawToken);

        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .tokenType("LINK_EMAIL")
                .pendingEmail("new@example.com")
                .pendingEmailIndex("new_idx_456")
                .expiresAt(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .build();

        when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(token));
        when(userRepository.existsByEmailIndexAndEmailVerifiedTrueAndIdNot("new_idx_456", userId)).thenReturn(false);

        User verified = emailVerificationService.verifyEmail(rawToken);

        assertThat(verified.getEmail()).isEqualTo("new@example.com");
        assertThat(verified.getEmailIndex()).isEqualTo("new_idx_456");
        assertThat(verified.isEmailVerified()).isTrue();
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).deleteAllByPendingEmailIndex("new_idx_456");
    }

    @Test
    void verifyEmailLinkThrowsWhenPendingEmailAlreadyVerifiedByAnotherUser() {
        String rawToken = "my-link-token";
        String tokenHash = hashToken(rawToken);

        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .tokenType("LINK_EMAIL")
                .pendingEmail("new@example.com")
                .pendingEmailIndex("new_idx_456")
                .expiresAt(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .build();

        when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(token));
        when(userRepository.existsByEmailIndexAndEmailVerifiedTrueAndIdNot("new_idx_456", userId)).thenReturn(true);

        assertThatThrownBy(() -> emailVerificationService.verifyEmail(rawToken))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("já foi verificado");

        assertThat(user.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, never()).save(any());
    }
}
