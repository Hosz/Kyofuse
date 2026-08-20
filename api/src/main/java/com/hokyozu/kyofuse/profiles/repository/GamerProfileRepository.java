package com.hokyozu.kyofuse.profiles.repository;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GamerProfileRepository extends JpaRepository<GamerProfile, UUID> {
    Optional<GamerProfile> findByUserId(UUID userId);

    List<GamerProfile> findByUserIdIn(List<UUID> userIds);

    /** Jogadores anunciando que procuram time, fora os ids excluídos (quem já está no time). */
    Page<GamerProfile> findByLookingForTeamTrueAndUser_StatusAndUserIdNotIn(
            UserStatus status, List<UUID> excludedUserIds, Pageable pageable);
}
