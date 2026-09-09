package com.hokyozu.kyofuse.profiles.repository;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GamerProfileRepository extends JpaRepository<GamerProfile, UUID>, JpaSpecificationExecutor<GamerProfile> {
    Optional<GamerProfile> findByUserId(UUID userId);

    @EntityGraph(attributePaths = {"user"})
    List<GamerProfile> findByUserIdIn(List<UUID> userIds);

    /** Jogadores anunciando que procuram time, fora os ids excluídos (quem já está no time). */
    Page<GamerProfile> findByLookingForTeamTrueAndUser_StatusAndUserIdNotIn(
            UserStatus status, List<UUID> excludedUserIds, Pageable pageable);

    Optional<GamerProfile> findByUserUsername(String username);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<GamerProfile> findAll(Specification<GamerProfile> specification, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    @Query("""
        SELECT gp FROM GamerProfile gp
        JOIN gp.user u
        WHERE u.status = :status
          AND u.id NOT IN :excludedUserIds
        ORDER BY
          CASE WHEN gp.lookingForTeam = true OR gp.lookingForDuo = true THEN 0 ELSE 1 END,
          gp.premierRating DESC NULLS LAST,
          gp.createdAt DESC
    """)
    Page<GamerProfile> findSuggestions(
            @Param("status") UserStatus status,
            @Param("excludedUserIds") Collection<UUID> excludedUserIds,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "user")
    @Query("""
        SELECT gp FROM GamerProfile gp
        JOIN gp.user u
        WHERE u.status = :status
          AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(gp.nickname) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY
          CASE WHEN LOWER(u.username) LIKE LOWER(CONCAT(:query, '%')) THEN 0 ELSE 1 END,
          u.username ASC
    """)
    List<GamerProfile> searchActiveProfilesForMention(
            @Param("query") String query,
            @Param("status") UserStatus status,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT gp FROM GamerProfile gp
    JOIN FETCH gp.user u
    LEFT JOIN FETCH gp.favoriteMaps fm
    WHERE gp.user.id = :userId
    """)
    Optional<GamerProfile> findFullByUserId(@Param("userId") UUID userId);

    @Query("""
    SELECT DISTINCT gp FROM GamerProfile gp
    JOIN FETCH gp.user u
    LEFT JOIN FETCH gp.favoriteMaps fm
    WHERE LOWER(u.username) = LOWER(:username)
    """)
    Optional<GamerProfile> findFullByUserUsername(@Param("username") String username);

    @EntityGraph(attributePaths = "user")
    @Query("""
        SELECT gp FROM GamerProfile gp
        JOIN gp.user u
        WHERE gp.premierRating IS NOT NULL
          AND u.status = com.hokyozu.kyofuse.users.enums.UserStatus.ACTIVE
    """)
    List<GamerProfile> findAllWithPremierRatingAndActiveUser();
}
