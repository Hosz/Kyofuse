package com.hokyozu.kyofuse.relationships.privacy.controller;

import com.hokyozu.kyofuse.relationships.privacy.dto.request.UserPrivacySettingsRequest;
import com.hokyozu.kyofuse.relationships.privacy.dto.response.UserPrivacySettingsResponse;
import com.hokyozu.kyofuse.relationships.privacy.service.UserPrivacySettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user-privacy-settings")
@RequiredArgsConstructor
public class UserPrivacySettingsController {

    private final UserPrivacySettingsService userPrivacySettingsService;

    @PutMapping("/set")
    public UserPrivacySettingsResponse setSettings(@AuthenticationPrincipal Jwt jwt,
                                                   @Valid @RequestBody UserPrivacySettingsRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userPrivacySettingsService.setSettings(userId, request);
    }

    @GetMapping("/get")
    public UserPrivacySettingsResponse getSettings(@AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return userPrivacySettingsService.getSettings(userId);
    }
}
