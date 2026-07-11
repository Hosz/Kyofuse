package com.hokyozu.kyofuse.teams.specification;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamRequiredRole;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class TeamSpecification {

    private TeamSpecification() {}

    public static Specification<Team> nameContains(String name) {
        return (root, query, cb) ->
                !hasText(name)
                        ? cb.conjunction()
                        : cb.like(
                                cb.lower(root.get("name")),
                        "%" + name.trim().toLowerCase(Locale.ROOT) + "%"
                );
    }

    public static Specification<Team> hasSlug(String slug) {
        return (root, query, cb) ->
                !hasText(slug)
                        ? cb.conjunction()
                        : cb.equal(
                            cb.lower(root.get("slug")),
                            slug.trim().toLowerCase(Locale.ROOT)
                );
    }

    public static Specification<Team> hasStatus(TeamStatus status) {
        return (root, query, cb) ->
                status == null
                        ? cb.notEqual(root.get("status"), TeamStatus.INACTIVE)
                        : cb.equal(root.get("status"), status);
    }

    public static Specification<Team> hasRegion(String region) {
        return (root, query, cb) ->
                !hasText(region)
                        ? cb.conjunction()
                        : cb.equal(
                                cb.lower(root.get("region")),
                                region.trim().toLowerCase(Locale.ROOT)
                );
    }

    public static Specification<Team> hasAnyRequiredRole(List<PlayerRole> requiredRoles) {
        return (root, query, cb) -> {
            if (requiredRoles == null || requiredRoles.isEmpty()) {
                return cb.conjunction();
            }

            Subquery<UUID> subquery = query.subquery(UUID.class);
            var role = subquery.from(TeamRequiredRole.class);

            subquery.select(role.get("team").get("id"))
                    .where(role.get("roleName").in(requiredRoles));

            return root.get("id").in(subquery);
        };
    }

    public static Specification<Team> minPremierRatingAtLeast(Integer rating) {
        return greaterThanOrEqualTo("minPremierRating", rating);
    }

    public static Specification<Team> maxPremierRatingAtMost(Integer rating) {
        return lessThanOrEqualTo("maxPremierRating", rating);
    }

    public static Specification<Team> minFaceitLevelAtLeast(Integer level) {
        return greaterThanOrEqualTo("minFaceitLevel", level);
    }

    public static Specification<Team> maxFaceitLevelAtMost(Integer level) {
        return lessThanOrEqualTo("maxFaceitLevel", level);
    }

    public static Specification<Team> minGcRankAtLeast(Integer rank) {
        return greaterThanOrEqualTo("minGcRank", rank);
    }

    public static Specification<Team> maxGcRankAtMost(Integer rank) {
        return lessThanOrEqualTo("maxGcRank", rank);
    }

    private static Specification<Team> greaterThanOrEqualTo(String field, Integer value) {
        return (root, query, cb) ->
                value == null
                        ? cb.conjunction()
                        : cb.greaterThanOrEqualTo(root.get(field), value);
    }

    private static Specification<Team> lessThanOrEqualTo(String field, Integer value) {
        return (root, query, cb) ->
                value == null
                        ? cb.conjunction()
                        : cb.lessThanOrEqualTo(root.get(field), value);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
