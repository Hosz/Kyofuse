package com.hokyozu.kyofuse.infrastructure.security.jwt;

import com.hokyozu.kyofuse.infrastructure.entity.AccountSwitchSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountSwitchSessionRepository extends JpaRepository<AccountSwitchSession, UUID> {

    Optional<AccountSwitchSession> findBySwitchTokenHash(String switchTokenHash);

    Optional<AccountSwitchSession> findByUserIdAndDeviceId(UUID userId, String deviceId);

    @Modifying
    @Query("DELETE FROM AccountSwitchSession s WHERE s.user.id = :userId AND s.deviceId = :deviceId")
    void deleteByUserIdAndDeviceId(@Param("userId") UUID userId, @Param("deviceId") String deviceId);

    @Modifying
    @Query("DELETE FROM AccountSwitchSession s WHERE s.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);
}
