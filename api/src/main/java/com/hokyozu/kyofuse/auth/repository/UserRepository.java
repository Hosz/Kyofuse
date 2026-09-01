package com.hokyozu.kyofuse.auth.repository;

import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmailIndex(String emailIndex);

    boolean existsByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIndex(String emailIndex);

    Optional<User> findByUsernameIgnoreCase(String username);
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.status = :status AND LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))")
    org.springframework.data.domain.Page<User> searchActiveUsers(@org.springframework.data.repository.query.Param("query") String query, @org.springframework.data.repository.query.Param("status") com.hokyozu.kyofuse.users.enums.UserStatus status, org.springframework.data.domain.Pageable pageable);

    Optional<User> findBySteamId(String steamId);

    Optional<User> findByGoogleId(String googleId);

    boolean existsBySteamId(String steamId);

    boolean existsByGoogleId(String googleId);

    Optional<User> findByUsername(String username);

    java.util.List<User> findByStatusAndDeactivatedAtBeforeAndDeletionScheduledAtIsNull(com.hokyozu.kyofuse.users.enums.UserStatus status, java.time.Instant cutoff);

    java.util.List<User> findByStatusAndDeletionScheduledAtBefore(com.hokyozu.kyofuse.users.enums.UserStatus status, java.time.Instant cutoff);
}
