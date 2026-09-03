package com.hokyozu.kyofuse.profiles.specification;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import com.hokyozu.kyofuse.relationships.privacy.entity.UserPrivacySettings;
import com.hokyozu.kyofuse.relationships.privacy.enums.ProfileVisibility;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

public final class ProfileSpecification {

    private ProfileSpecification() {}

    public static Specification<GamerProfile> usernameContains(String username) {
        return (root, query, cb) ->
                !hasText(username)
                        ? cb.conjunction()
                        : cb.or(
                                cb.like(cb.lower(root.join("user").get("username")), "%" + username.trim().toLowerCase(Locale.ROOT) + "%"),
                                cb.like(cb.lower(root.get("nickname")), "%" + username.trim().toLowerCase(Locale.ROOT) + "%")
                        );
    }

    public static Specification<GamerProfile> hasUserStatus(UserStatus status) {
        return (root, query, cb) ->
                status == null
                        ? cb.equal(root.join("user").get("status"), UserStatus.ACTIVE)
                        : cb.equal(root.join("user").get("status"), status);
    }

    public static Specification<GamerProfile> notInUserIds(Collection<UUID> excludedIds) {
        return (root, query, cb) -> {
            if (excludedIds == null || excludedIds.isEmpty()) {
                return cb.conjunction();
            }
            return cb.not(root.join("user").get("id").in(excludedIds));
        };
    }

    public static Specification<GamerProfile> visibleTo(UUID viewerId) {
        return (root, query, cb) -> {
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<UserPrivacySettings> settingsRoot = subquery.from(UserPrivacySettings.class);
            subquery.select(settingsRoot.get("id"));
            subquery.where(cb.equal(settingsRoot.get("profileVisibility"), ProfileVisibility.PRIVATE));

            Predicate notPrivate = cb.not(root.join("user").get("id").in(subquery));
            if (viewerId != null) {
                return cb.or(cb.equal(root.join("user").get("id"), viewerId), notPrivate);
            }
            return notPrivate;
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
