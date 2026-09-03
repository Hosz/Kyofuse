package com.hokyozu.kyofuse.communities.repository;

import com.hokyozu.kyofuse.communities.entity.UserPinnedCommunity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPinnedCommunityRepository extends JpaRepository<UserPinnedCommunity, UUID> {
    // Sem paginação de propósito: é a barra de abas do feed, uma lista curta por definição.
    @EntityGraph(attributePaths = {"community", "community.owner"})
    List<UserPinnedCommunity> findByUserIdOrderByPositionAsc(UUID userId);

    Optional<UserPinnedCommunity> findByUserIdAndCommunityId(UUID userId, UUID communityId);

    boolean existsByUserIdAndCommunityId(UUID userId, UUID communityId);

    int countByUserId(UUID userId);
}
