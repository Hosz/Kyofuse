package com.hokyozu.kyofuse.relationships.privacy.repository;

import com.hokyozu.kyofuse.relationships.privacy.entity.UserPrivacySettings;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserPrivacySettingsRepository extends JpaRepository<UserPrivacySettings, UUID> {
    UserPrivacySettings findByUser(User user);

    boolean existsByUser(User user);

    List<UserPrivacySettings> findAllByUserIn(List<User> users);
}
