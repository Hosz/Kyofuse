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

    Optional<User> findBySteamId(String steamId);
}
