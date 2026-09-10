package com.hokyozu.kyofuse.auth.repository;

import com.hokyozu.kyofuse.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    List<UserSession> findAllByUserIdAndRevokedFalseOrderByLastActiveAtDesc(UUID userId);

    Optional<UserSession> findByUserIdAndDeviceIdAndRevokedFalse(UUID userId, String deviceId);

    Optional<UserSession> findFirstByUserIdAndDeviceIdOrderByCreatedAtDesc(UUID userId, String deviceId);

    Optional<UserSession> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndDeviceIdAndTrustedTrueAndRevokedFalse(UUID userId, String deviceId);

    boolean existsByUserIdAndDeviceIdAndTrustedTrue(UUID userId, String deviceId);

    @Modifying
    @Query("""
        UPDATE UserSession s
        SET s.revoked = true, s.revokedAt = :now
        WHERE s.user.id = :userId AND s.deviceId != :currentDeviceId AND s.revoked = false
    """)
    int revokeAllByUserIdExceptDeviceId(
            @Param("userId") UUID userId,
            @Param("currentDeviceId") String currentDeviceId,
            @Param("now") Instant now
    );

    @Modifying
    @Query("""
        UPDATE UserSession s
        SET s.revoked = true, s.revokedAt = :now
        WHERE s.id = :sessionId AND s.user.id = :userId AND s.revoked = false
    """)
    int revokeByIdAndUserId(
            @Param("sessionId") UUID sessionId,
            @Param("userId") UUID userId,
            @Param("now") Instant now
    );
}
