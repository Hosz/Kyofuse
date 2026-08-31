package com.hokyozu.kyofuse.infrastructure.security.jwt;

import com.hokyozu.kyofuse.infrastructure.entity.AccountSwitchSession;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountSwitchServiceTest {

    @Mock
    private AccountSwitchSessionRepository sessionRepository;

    @InjectMocks
    private AccountSwitchService accountSwitchService;

    private User user;
    private UUID userId;
    private String deviceId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        deviceId = "device-123";
        user = User.builder()
                .id(userId)
                .username("player1")
                .email("player1@example.com")
                .build();
    }

    @Test
    @DisplayName("Deve criar nova sessão quando deviceId for fornecido e não existir sessão prévia")
    void createOrUpdateSession_createsNewSession() {
        when(sessionRepository.findByUserIdAndDeviceId(userId, deviceId)).thenReturn(Optional.empty());

        String rawToken = accountSwitchService.createOrUpdateSession(user, deviceId);

        assertThat(rawToken).isNotBlank();
        ArgumentCaptor<AccountSwitchSession> captor = ArgumentCaptor.forClass(AccountSwitchSession.class);
        verify(sessionRepository).save(captor.capture());

        AccountSwitchSession saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getDeviceId()).isEqualTo(deviceId);
        assertThat(saved.getSwitchTokenHash()).isNotBlank();
        assertThat(saved.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("Deve atualizar sessão existente quando já existir uma para o usuário e dispositivo")
    void createOrUpdateSession_updatesExistingSession() {
        AccountSwitchSession existing = AccountSwitchSession.builder()
                .id(UUID.randomUUID())
                .user(user)
                .deviceId(deviceId)
                .switchTokenHash("oldhash")
                .expiresAt(Instant.now().plus(10, ChronoUnit.DAYS))
                .createdAt(Instant.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(Instant.now().minus(10, ChronoUnit.DAYS))
                .build();

        when(sessionRepository.findByUserIdAndDeviceId(userId, deviceId)).thenReturn(Optional.of(existing));

        String rawToken = accountSwitchService.createOrUpdateSession(user, deviceId);

        assertThat(rawToken).isNotBlank();
        verify(sessionRepository).save(existing);
        assertThat(existing.getSwitchTokenHash()).isNotEqualTo("oldhash");
    }

    @Test
    @DisplayName("Deve retornar null quando deviceId for nulo ou em branco")
    void createOrUpdateSession_returnsNull_whenDeviceIdBlank() {
        String token = accountSwitchService.createOrUpdateSession(user, "   ");
        assertThat(token).isNull();
        verifyNoInteractions(sessionRepository);
    }

    @Test
    @DisplayName("Deve validar e rotacionar switch token com sucesso")
    void validateAndRotate_success() {
        String rawToken = "valid-raw-token";
        AccountSwitchSession session = AccountSwitchSession.builder()
                .id(UUID.randomUUID())
                .user(user)
                .deviceId(deviceId)
                .switchTokenHash("hash")
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(sessionRepository.findBySwitchTokenHash(anyString())).thenReturn(Optional.of(session));

        AccountSwitchService.SwitchValidationResult result = accountSwitchService.validateAndRotate(userId, rawToken, deviceId);

        assertThat(result.user()).isEqualTo(user);
        assertThat(result.newSwitchToken()).isNotBlank();
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando token não for encontrado")
    void validateAndRotate_notFound_throwsUnauthorized() {
        when(sessionRepository.findBySwitchTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountSwitchService.validateAndRotate(userId, "invalid-token", deviceId))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("não encontrada");
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException e deletar sessão se usuário não bater")
    void validateAndRotate_userMismatch_throwsUnauthorizedAndDeletes() {
        User anotherUser = User.builder().id(UUID.randomUUID()).build();
        AccountSwitchSession session = AccountSwitchSession.builder()
                .user(anotherUser)
                .deviceId(deviceId)
                .expiresAt(Instant.now().plus(10, ChronoUnit.DAYS))
                .build();

        when(sessionRepository.findBySwitchTokenHash(anyString())).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> accountSwitchService.validateAndRotate(userId, "token", deviceId))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("não corresponde");

        verify(sessionRepository).delete(session);
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException e deletar sessão quando expirada")
    void validateAndRotate_expired_throwsUnauthorizedAndDeletes() {
        AccountSwitchSession session = AccountSwitchSession.builder()
                .user(user)
                .deviceId(deviceId)
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();

        when(sessionRepository.findBySwitchTokenHash(anyString())).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> accountSwitchService.validateAndRotate(userId, "token", deviceId))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expirada");

        verify(sessionRepository).delete(session);
    }

    @Test
    @DisplayName("Deve revogar sessão específica de um usuário em um dispositivo")
    void revokeSession_callsDeleteByUserIdAndDeviceId() {
        accountSwitchService.revokeSession(userId, deviceId);
        verify(sessionRepository).deleteByUserIdAndDeviceId(userId, deviceId);
    }

    @Test
    @DisplayName("Deve revogar todas as sessões de um usuário")
    void revokeAllForUser_callsDeleteAllByUserId() {
        accountSwitchService.revokeAllForUser(userId);
        verify(sessionRepository).deleteAllByUserId(userId);
    }
}
