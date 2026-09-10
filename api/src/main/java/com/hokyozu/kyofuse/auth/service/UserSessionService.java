package com.hokyozu.kyofuse.auth.service;

import com.hokyozu.kyofuse.auth.dto.response.UserSessionResponse;
import com.hokyozu.kyofuse.auth.entity.UserSession;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.auth.repository.UserSessionRepository;
import com.hokyozu.kyofuse.infrastructure.client.DeviceInfo;
import com.hokyozu.kyofuse.infrastructure.client.UserAgentParser;
import com.hokyozu.kyofuse.infrastructure.geolocation.GeoLocationService;
import com.hokyozu.kyofuse.infrastructure.geolocation.LocationInfo;
import com.hokyozu.kyofuse.infrastructure.security.jwt.RefreshTokenRepository;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;
    private final UserAgentParser userAgentParser;
    private final GeoLocationService geoLocationService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserSession recordOrUpdateSession(User user, String deviceId, String clientIp, String userAgent, LocationInfo explicitLocation) {
        if (user == null || user.getId() == null) {
            return null;
        }

        String effectiveDeviceId = (deviceId != null && !deviceId.isBlank())
                ? deviceId.trim()
                : "dev_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        DeviceInfo device = userAgentParser.parse(userAgent);
        String deviceType = resolveDeviceType(device.operatingSystem());

        LocationInfo location = explicitLocation;
        if (location == null && geoLocationService != null) {
            try {
                location = geoLocationService.resolveLocation(clientIp);
            } catch (Exception e) {
                log.debug("[UserSession] Não foi possível resolver localização: {}", e.getMessage());
            }
        }

        String locationStr = location != null ? location.formattedLocation() : "Localização desconhecida";

        Optional<UserSession> existingOpt = userSessionRepository
                .findFirstByUserIdAndDeviceIdOrderByCreatedAtDesc(user.getId(), effectiveDeviceId);

        if (existingOpt.isPresent()) {
            UserSession session = existingOpt.get();
            session.setLastActiveAt(Instant.now());
            session.setIpAddress(clientIp);
            session.setLocation(locationStr);
            session.setDeviceType(deviceType);
            session.setDeviceName(device.summary());
            session.setBrowser(device.browser());
            session.setOs(device.operatingSystem());
            if (session.isRevoked()) {
                session.setRevoked(false);
                session.setRevokedAt(null);
            }
            return userSessionRepository.save(session);
        }

        UserSession newSession = UserSession.builder()
                .user(user)
                .deviceId(effectiveDeviceId)
                .deviceType(deviceType)
                .deviceName(device.summary())
                .browser(device.browser())
                .os(device.operatingSystem())
                .ipAddress(clientIp)
                .location(locationStr)
                .trusted(false)
                .lastActiveAt(Instant.now())
                .createdAt(Instant.now())
                .revoked(false)
                .build();

        return userSessionRepository.save(newSession);
    }

    @Transactional(readOnly = true)
    public boolean isDeviceTrusted(UUID userId, String deviceId) {
        if (userId == null || deviceId == null || deviceId.isBlank()) {
            return false;
        }
        return userSessionRepository.existsByUserIdAndDeviceIdAndTrustedTrue(userId, deviceId.trim());
    }

    @Transactional
    public void trustDevice(UUID userId, String deviceId) {
        if (userId == null || deviceId == null || deviceId.isBlank()) {
            return;
        }

        userSessionRepository.findFirstByUserIdAndDeviceIdOrderByCreatedAtDesc(userId, deviceId.trim())
                .ifPresentOrElse(session -> {
                    session.setTrusted(true);
                    session.setTrustedAt(Instant.now());
                    userSessionRepository.save(session);
                    log.info("[UserSession] Dispositivo {} marcado como confiável para usuário {}", deviceId, userId);
                }, () -> {
                    log.warn("[UserSession] Tentativa de confiar em dispositivo não encontrado: {} para usuário {}", deviceId, userId);
                });
    }

    @Transactional
    public void untrustDevice(UUID userId, UUID sessionId) {
        UserSession session = userSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new NotFoundException("Sessão não encontrada"));

        session.setTrusted(false);
        session.setTrustedAt(null);
        userSessionRepository.save(session);
    }

    @Transactional
    public List<UserSessionResponse> listActiveSessions(UUID userId, String currentDeviceId, String clientIp, String userAgent) {
        if (userId != null && userRepository != null) {
            userRepository.findById(userId).ifPresent(user -> {
                recordOrUpdateSession(user, currentDeviceId, clientIp, userAgent, null);
            });
        }
        return listActiveSessions(userId, currentDeviceId);
    }

    @Transactional(readOnly = true)
    public List<UserSessionResponse> listActiveSessions(UUID userId, String currentDeviceId) {
        String cleanCurrent = currentDeviceId != null ? currentDeviceId.trim() : "";

        List<UserSession> sessions = userSessionRepository.findAllByUserIdAndRevokedFalseOrderByLastActiveAtDesc(userId);
        boolean hasCurrent = sessions.stream().anyMatch(s -> !cleanCurrent.isEmpty() && cleanCurrent.equalsIgnoreCase(s.getDeviceId()));

        return sessions.stream()
                .map(s -> {
                    boolean isCurrent = hasCurrent
                            ? cleanCurrent.equalsIgnoreCase(s.getDeviceId())
                            : (sessions.indexOf(s) == 0);

                    return UserSessionResponse.builder()
                            .id(s.getId())
                            .deviceId(s.getDeviceId())
                            .deviceType(s.getDeviceType())
                            .deviceName(s.getDeviceName())
                            .browser(s.getBrowser())
                            .os(s.getOs())
                            .ipAddress(maskIp(s.getIpAddress()))
                            .location(s.getLocation())
                            .trusted(s.isTrusted())
                            .trustedAt(s.getTrustedAt())
                            .lastActiveAt(s.getLastActiveAt())
                            .createdAt(s.getCreatedAt())
                            .current(isCurrent)
                            .build();
                })
                .toList();
    }

    @Transactional
    public void revokeSession(UUID userId, UUID sessionId) {
        UserSession session = userSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new NotFoundException("Sessão não encontrada"));

        session.setRevoked(true);
        session.setRevokedAt(Instant.now());
        userSessionRepository.save(session);
        log.info("[UserSession] Sessão {} revogada para o usuário {}", sessionId, userId);
    }

    @Transactional
    public void revokeAllOtherSessions(UUID userId, String currentDeviceId) {
        if (currentDeviceId == null || currentDeviceId.isBlank()) {
            return;
        }
        int count = userSessionRepository.revokeAllByUserIdExceptDeviceId(userId, currentDeviceId.trim(), Instant.now());
        log.info("[UserSession] {} outras sessões revogadas para o usuário {}", count, userId);
    }

    private String resolveDeviceType(String os) {
        if (os == null) return "UNKNOWN";
        String lower = os.toLowerCase(Locale.ROOT);
        if (lower.contains("windows")) return "WINDOWS";
        if (lower.contains("ios") || lower.contains("iphone") || lower.contains("ipad")) return "IOS";
        if (lower.contains("android")) return "ANDROID";
        if (lower.contains("mac")) return "MACOS";
        if (lower.contains("linux")) return "LINUX";
        return "UNKNOWN";
    }

    private String maskIp(String ip) {
        if (ip == null || ip.isBlank() || ip.equals("desconhecido") || ip.equals("127.0.0.1") || ip.equals("::1")) {
            return ip;
        }
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) {
            return ip.substring(0, lastDot) + ".***";
        }
        return ip;
    }
}
