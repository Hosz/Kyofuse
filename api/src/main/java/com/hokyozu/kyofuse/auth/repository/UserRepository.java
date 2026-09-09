package com.hokyozu.kyofuse.auth.repository;

import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmailIndex(String emailIndex);

    boolean existsByEmailIndexAndEmailVerifiedTrue(String emailIndex);

    boolean existsByEmailIndexAndEmailVerifiedTrueAndIdNot(String emailIndex, UUID id);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndEmailVerifiedTrue(String username);

    boolean existsByUsernameIgnoreCaseAndEmailVerifiedTrueAndIdNot(String username, UUID id);

    Optional<User> findFirstByEmailIndexOrderByEmailVerifiedDescCreatedAtDesc(String emailIndex);

    default Optional<User> findByEmailIndex(String emailIndex) {
        return findFirstByEmailIndexOrderByEmailVerifiedDescCreatedAtDesc(emailIndex);
    }

    Optional<User> findFirstByUsernameIgnoreCaseOrderByEmailVerifiedDescCreatedAtDesc(String username);

    default Optional<User> findByUsernameIgnoreCase(String username) {
        return findFirstByUsernameIgnoreCaseOrderByEmailVerifiedDescCreatedAtDesc(username);
    }

    Optional<User> findFirstByUsernameOrderByEmailVerifiedDescCreatedAtDesc(String username);

    default Optional<User> findByUsername(String username) {
        return findFirstByUsernameOrderByEmailVerifiedDescCreatedAtDesc(username);
    }

    java.util.List<User> findAllByEmailIndexAndEmailVerifiedFalse(String emailIndex);

    java.util.List<User> findAllByUsernameIgnoreCaseAndEmailVerifiedFalse(String username);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.status = :status AND LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))")
    org.springframework.data.domain.Page<User> searchActiveUsers(@org.springframework.data.repository.query.Param("query") String query, @org.springframework.data.repository.query.Param("status") com.hokyozu.kyofuse.users.enums.UserStatus status, org.springframework.data.domain.Pageable pageable);

    Optional<User> findBySteamId(String steamId);

    Optional<User> findByGoogleId(String googleId);

    boolean existsBySteamId(String steamId);

    boolean existsByGoogleId(String googleId);

    java.util.List<User> findByStatusAndDeactivatedAtBeforeAndDeletionScheduledAtIsNull(com.hokyozu.kyofuse.users.enums.UserStatus status, java.time.Instant cutoff);

    java.util.List<User> findByStatusAndDeletionScheduledAtBefore(com.hokyozu.kyofuse.users.enums.UserStatus status, java.time.Instant cutoff);
}
