package com.hokyozu.kyofuse.infrastructure.security.totp;

import com.hokyozu.kyofuse.infrastructure.entity.RecoveryCode;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RecoveryCodeRepository extends JpaRepository<RecoveryCode, UUID> {

    Optional<RecoveryCode> findByUserAndCodeHashAndUsedAtIsNull(User user, String codeHash);

    void deleteAllByUser(User user);
}
