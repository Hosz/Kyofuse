package com.hokyozu.kyofuse.profiles.controller;

import com.hokyozu.kyofuse.profiles.dto.request.GamerProfileRequest;
import com.hokyozu.kyofuse.profiles.dto.response.GamerProfileResponse;
import com.hokyozu.kyofuse.profiles.service.GamerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class GamerProfileController {

    private final GamerProfileService gamerProfileService;

    @PatchMapping("/edit")
    @ResponseStatus(HttpStatus.OK)
    public GamerProfileResponse editProfile(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid GamerProfileRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return gamerProfileService.editProfile(userId, request);
    }

    @GetMapping("/me")
    public GamerProfileResponse myProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return gamerProfileService.viewMyProfile(userId);
    }

    @GetMapping("/{profileId}")
    public GamerProfileResponse userProfile(@PathVariable UUID profileId) {
        return gamerProfileService.viewUserProfile(profileId);
    }
}
