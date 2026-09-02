package com.hokyozu.kyofuse.infrastructure.security.jwt;

import com.hokyozu.kyofuse.infrastructure.entity.RefreshToken;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository);
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationDays", 30L);
        user = User.builder().id(UUID.randomUUID()).role(UserRole.USER).build();
    }

    @Test
    void issuePersistsHashedTokenAndReturnsRawToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenService.IssuedToken issued = refreshTokenService.issue(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken persisted = captor.getValue();
        assertThat(persisted.getTokenHash()).isNotEqualTo(issued.rawToken());
        assertThat(persisted.getTokenHash()).hasSize(64);
        assertThat(persisted.isRevoked()).isFalse();
        assertThat(persisted.getUser()).isEqualTo(user);
        assertThat(issued.expiresAt()).isAfter(Instant.now());
    }

    @Test
    void rotateRevokesCurrentTokenAndIssuesNewOneInSameFamily() {
        UUID familyId = UUID.randomUUID();
        RefreshToken current = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash("current-hash")
                .familyId(familyId)
                .revoked(false)
                .expiresAt(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .build();

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(current));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenService.RotationResult rotation = refreshTokenService.rotate("raw-token");

        assertThat(rotation.user()).isEqualTo(user);
        assertThat(current.isRevoked()).isTrue();
        verify(refreshTokenRepository, org.mockito.Mockito.never()).revokeAllByFamilyId(any());

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        RefreshToken newToken = captor.getAllValues().get(0);
        assertThat(newToken.getFamilyId()).isEqualTo(familyId);
        assertThat(newToken.getTokenHash()).isNotEqualTo(current.getTokenHash());
    }

    @Test
    void rotateWithAlreadyRevokedTokenRevokesFamilyAndThrows() {
        UUID familyId = UUID.randomUUID();
        RefreshToken current = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash("current-hash")
                .familyId(familyId)
                .revoked(true)
                .expiresAt(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .build();

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(current));

        assertThatThrownBy(() -> refreshTokenService.rotate("raw-token"))
                .isInstanceOf(UnauthorizedException.class);

        verify(refreshTokenRepository).revokeAllByFamilyId(familyId);
    }

    @Test
    void rotateWithExpiredTokenThrows() {
        RefreshToken current = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash("current-hash")
                .familyId(UUID.randomUUID())
                .revoked(false)
                .expiresAt(Instant.now().minusSeconds(1))
                .createdAt(Instant.now())
                .build();

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(current));

        assertThatThrownBy(() -> refreshTokenService.rotate("raw-token"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void rotateWithUnknownTokenThrows() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.rotate("raw-token"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void revokeMarksMatchingTokenAsRevoked() {
        RefreshToken current = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash("current-hash")
                .familyId(UUID.randomUUID())
                .revoked(false)
                .expiresAt(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .build();

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(current));

        refreshTokenService.revoke("raw-token");

        assertThat(current.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(current);
    }

    @Test
    void revokeIgnoresUnknownToken() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        refreshTokenService.revoke("raw-token");

        verify(refreshTokenRepository, org.mockito.Mockito.never()).save(any());
    }
}
