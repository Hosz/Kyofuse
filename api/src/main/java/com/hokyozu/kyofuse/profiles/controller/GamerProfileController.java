package com.hokyozu.kyofuse.profiles.controller;

import com.hokyozu.kyofuse.profiles.dto.request.GamerProfileRequest;
import com.hokyozu.kyofuse.profiles.dto.response.GamerProfileResponse;
import com.hokyozu.kyofuse.profiles.service.GamerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class GamerProfileController {

    private final GamerProfileService gamerProfileService;

    @PatchMapping("/edit")
    @ResponseStatus(HttpStatus.OK)
    public GamerProfileResponse editProfile(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid GamerProfileRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return gamerProfileService.editProfile(userId, request);
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public GamerProfileResponse uploadAvatar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        UUID userId = UUID.fromString(jwt.getSubject());
        return gamerProfileService.uploadAvatar(userId, file);
    }

    @PostMapping(value = "/me/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public GamerProfileResponse uploadBanner(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        UUID userId = UUID.fromString(jwt.getSubject());
        return gamerProfileService.uploadBanner(userId, file);
    }

    @GetMapping("/me")
    public GamerProfileResponse myProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return gamerProfileService.viewMyProfile(userId);
    }

    @GetMapping("/{profileId}")
    public GamerProfileResponse userProfile(@AuthenticationPrincipal Jwt jwt,
                                            @PathVariable UUID profileId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return gamerProfileService.viewUserProfile(profileId, userId);
    }
}
