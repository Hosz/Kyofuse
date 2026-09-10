package com.hokyozu.kyofuse.auth.repository;

import com.hokyozu.kyofuse.auth.entity.EmailVerificationToken;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    @EntityGraph(attributePaths = {"user"})
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);
    void deleteAllByUser(User user);
    void deleteAllByPendingEmailIndex(String pendingEmailIndex);
    java.util.List<EmailVerificationToken> findAllByPendingEmailIndex(String pendingEmailIndex);
}
