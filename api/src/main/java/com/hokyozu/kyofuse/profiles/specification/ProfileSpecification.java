package com.hokyozu.kyofuse.profiles.specification;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

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

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
