package com.hokyozu.kyofuse.profiles.repository;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GamerProfileRepository extends JpaRepository<GamerProfile, UUID> {
    Optional<GamerProfile> findByUserId(UUID userId);
}
