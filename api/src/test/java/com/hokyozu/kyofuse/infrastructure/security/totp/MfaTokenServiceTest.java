package com.hokyozu.kyofuse.infrastructure.security.totp;

import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MfaTokenServiceTest {

    @Mock
    private MfaSessionRepository mfaSessionRepository;

    @InjectMocks
    private MfaTokenService mfaTokenService;

    @Test
    void generateCreatesAndSavesSessionInRedis() {
        User user = User.builder().id(UUID.randomUUID()).role(UserRole.USER).build();

        String token = mfaTokenService.generate(user);

        assertThat(token).isNotBlank();
        ArgumentCaptor<MfaSession> captor = ArgumentCaptor.forClass(MfaSession.class);
        verify(mfaSessionRepository).save(captor.capture());
        MfaSession saved = captor.getValue();
        assertThat(saved.getMfaToken()).isEqualTo(token);
        assertThat(saved.getUserId()).isEqualTo(user.getId());
        assertThat(saved.getRemainingAttempts()).isEqualTo(3);
    }

    @Test
    void resolveAndValidateUserIdReturnsUserIdWhenSessionExists() {
        UUID userId = UUID.randomUUID();
        MfaSession session = MfaSession.builder()
                .mfaToken("valid-token")
                .userId(userId)
                .remainingAttempts(3)
                .createdAt(Instant.now())
                .build();

        when(mfaSessionRepository.findById("valid-token")).thenReturn(Optional.of(session));

        UUID resolved = mfaTokenService.resolveAndValidateUserId("valid-token");
        assertThat(resolved).isEqualTo(userId);
    }

    @Test
    void resolveAndValidateUserIdThrowsWhenNotFound() {
        when(mfaSessionRepository.findById("invalid-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mfaTokenService.resolveAndValidateUserId("invalid-token"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void recordFailedAttemptDecrementsAndDeletesWhenExhausted() {
        MfaSession session = MfaSession.builder()
                .mfaToken("test-token")
                .userId(UUID.randomUUID())
                .remainingAttempts(1)
                .build();

        when(mfaSessionRepository.findById("test-token")).thenReturn(Optional.of(session));

        mfaTokenService.recordFailedAttempt("test-token");

        verify(mfaSessionRepository).deleteById("test-token");
    }

    @Test
    void consumeDeletesSession() {
        mfaTokenService.consume("some-token");
        verify(mfaSessionRepository).deleteById("some-token");
    }
}
