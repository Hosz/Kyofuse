package com.hokyozu.kyofuse.auth.service;

import com.hokyozu.kyofuse.auth.dto.response.UserSessionResponse;
import com.hokyozu.kyofuse.auth.entity.UserSession;
import com.hokyozu.kyofuse.auth.repository.UserSessionRepository;
import com.hokyozu.kyofuse.infrastructure.client.DeviceInfo;
import com.hokyozu.kyofuse.infrastructure.client.UserAgentParser;
import com.hokyozu.kyofuse.infrastructure.geolocation.GeoLocationService;
import com.hokyozu.kyofuse.infrastructure.geolocation.LocationInfo;
import com.hokyozu.kyofuse.infrastructure.security.jwt.RefreshTokenRepository;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSessionServiceTest {

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private UserAgentParser userAgentParser;

    @Mock
    private GeoLocationService geoLocationService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserSessionService userSessionService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder().id(userId).username("testuser").email("test@example.com").build();
    }

    @Test
    void recordOrUpdateSession_createsNewSessionWhenNoneExists() {
        String deviceId = "dev-123";
        String ip = "192.168.1.1";
        String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
        LocationInfo location = LocationInfo.of(ip, "São Paulo", "SP", "Brasil", "BR");

        when(userAgentParser.parse(ua)).thenReturn(new DeviceInfo("Chrome", "Windows", "Computador", "Chrome no Windows"));
        when(userSessionRepository.findFirstByUserIdAndDeviceIdOrderByCreatedAtDesc(userId, deviceId))
                .thenReturn(Optional.empty());
        when(userSessionRepository.save(any(UserSession.class))).thenAnswer(inv -> inv.getArgument(0));

        UserSession session = userSessionService.recordOrUpdateSession(user, deviceId, ip, ua, location);

        assertThat(session).isNotNull();
        assertThat(session.getDeviceId()).isEqualTo(deviceId);
        assertThat(session.getDeviceType()).isEqualTo("WINDOWS");
        assertThat(session.getDeviceName()).isEqualTo("Chrome no Windows");
        assertThat(session.isTrusted()).isFalse();
        assertThat(session.isRevoked()).isFalse();
        verify(userSessionRepository).save(any(UserSession.class));
    }

    @Test
    void recordOrUpdateSession_updatesExistingSession() {
        String deviceId = "dev-123";
        String ip = "192.168.1.1";
        String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
        LocationInfo location = LocationInfo.of(ip, "São Paulo", "SP", "Brasil", "BR");

        UserSession existing = UserSession.builder()
                .id(UUID.randomUUID())
                .user(user)
                .deviceId(deviceId)
                .deviceType("WINDOWS")
                .trusted(true)
                .lastActiveAt(Instant.now().minusSeconds(3600))
                .createdAt(Instant.now().minusSeconds(86400))
                .revoked(true)
                .build();

        when(userAgentParser.parse(ua)).thenReturn(new DeviceInfo("Chrome", "Windows", "Computador", "Chrome no Windows"));
        when(userSessionRepository.findFirstByUserIdAndDeviceIdOrderByCreatedAtDesc(userId, deviceId))
                .thenReturn(Optional.of(existing));
        when(userSessionRepository.save(any(UserSession.class))).thenAnswer(inv -> inv.getArgument(0));

        UserSession session = userSessionService.recordOrUpdateSession(user, deviceId, ip, ua, location);

        assertThat(session).isNotNull();
        assertThat(session.isRevoked()).isFalse();
        assertThat(session.isTrusted()).isTrue();
        verify(userSessionRepository).save(existing);
    }

    @Test
    void trustDevice_setsTrustedTrue() {
        String deviceId = "dev-123";
        UserSession existing = UserSession.builder()
                .id(UUID.randomUUID())
                .user(user)
                .deviceId(deviceId)
                .trusted(false)
                .build();

        when(userSessionRepository.findFirstByUserIdAndDeviceIdOrderByCreatedAtDesc(userId, deviceId))
                .thenReturn(Optional.of(existing));

        userSessionService.trustDevice(userId, deviceId);

        assertThat(existing.isTrusted()).isTrue();
        assertThat(existing.getTrustedAt()).isNotNull();
        verify(userSessionRepository).save(existing);
    }

    @Test
    void listActiveSessions_identifiesCurrentDevice() {
        String currentDeviceId = "dev-current";
        String otherDeviceId = "dev-other";

        UserSession s1 = UserSession.builder()
                .id(UUID.randomUUID())
                .deviceId(currentDeviceId)
                .deviceType("WINDOWS")
                .deviceName("Chrome no Windows")
                .ipAddress("187.54.12.34")
                .location("São Paulo, SP, Brasil")
                .trusted(true)
                .lastActiveAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        UserSession s2 = UserSession.builder()
                .id(UUID.randomUUID())
                .deviceId(otherDeviceId)
                .deviceType("ANDROID")
                .deviceName("Chrome no Android")
                .ipAddress("177.32.45.67")
                .location("Rio de Janeiro, RJ, Brasil")
                .trusted(false)
                .lastActiveAt(Instant.now().minusSeconds(1800))
                .createdAt(Instant.now().minusSeconds(7200))
                .build();

        when(userSessionRepository.findAllByUserIdAndRevokedFalseOrderByLastActiveAtDesc(userId))
                .thenReturn(List.of(s1, s2));

        List<UserSessionResponse> sessions = userSessionService.listActiveSessions(userId, currentDeviceId);

        assertThat(sessions).hasSize(2);
        assertThat(sessions.get(0).current()).isTrue();
        assertThat(sessions.get(0).ipAddress()).isEqualTo("187.54.12.***");
        assertThat(sessions.get(1).current()).isFalse();
    }

    @Test
    void revokeSession_marksRevoked() {
        UUID sessionId = UUID.randomUUID();
        UserSession session = UserSession.builder()
                .id(sessionId)
                .user(user)
                .revoked(false)
                .build();

        when(userSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));

        userSessionService.revokeSession(userId, sessionId);

        assertThat(session.isRevoked()).isTrue();
        assertThat(session.getRevokedAt()).isNotNull();
        verify(userSessionRepository).save(session);
    }

    @Test
    void revokeAllOtherSessions_callsRepository() {
        String currentDeviceId = "dev-current";

        userSessionService.revokeAllOtherSessions(userId, currentDeviceId);

        verify(userSessionRepository).revokeAllByUserIdExceptDeviceId(eq(userId), eq(currentDeviceId), any(Instant.class));
    }
}
