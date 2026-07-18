package com.hokyozu.kyofuse.relationships.privacy.service;

import com.hokyozu.kyofuse.relationships.privacy.dto.request.UserPrivacySettingsRequest;
import com.hokyozu.kyofuse.relationships.privacy.dto.response.UserPrivacySettingsResponse;
import com.hokyozu.kyofuse.relationships.privacy.entity.UserPrivacySettings;
import com.hokyozu.kyofuse.relationships.privacy.mapper.UserPrivacySettingsMapper;
import com.hokyozu.kyofuse.relationships.privacy.repository.UserPrivacySettingsRepository;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserPrivacySettingsService {

    private final UserFinder userFinder;
    private final UserChecker userChecker;
    private final UserPrivacySettingsRepository userPrivacySettingsRepository;

    @Transactional
    public UserPrivacySettingsResponse setSettings(UUID userId, @Valid UserPrivacySettingsRequest request) {
        User user = userFinder.findProfileByUserId(userId);
        UserPrivacySettings settings = userPrivacySettingsRepository.findByUser(user);
        userChecker.checkActive(user);

        if (!user.getId().equals(userId)) {
            throw new ForbiddenException("User does not have permission to update these settings.");
        }

        UserPrivacySettingsMapper.toUpdate(settings, request);
        return UserPrivacySettingsMapper.toResponse(settings);
    }

    @Transactional(readOnly = true)
    public UserPrivacySettingsResponse getSettings(UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        UserPrivacySettings settings = userPrivacySettingsRepository.findByUser(user);
        userChecker.checkActive(user);

        if (!user.getId().equals(userId)) {
            throw new ForbiddenException("User does not have permission to view these settings.");
        }

        return UserPrivacySettingsMapper.toResponse(settings);
    }

    @Transactional
    public void createDefault(User user) {
        if (userPrivacySettingsRepository.existsByUser(user)) {
            throw new ForbiddenException("User already has privacy settings.");
        }

        UserPrivacySettings settings = UserPrivacySettingsMapper.createDefault(user);
        userPrivacySettingsRepository.save(settings);
    }
}
