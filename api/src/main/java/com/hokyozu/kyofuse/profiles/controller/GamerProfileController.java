package com.hokyozu.kyofuse.profiles.controller;

import com.hokyozu.kyofuse.profiles.dto.request.GamerProfileRequest;
import com.hokyozu.kyofuse.profiles.dto.request.ProfileFilter;
import com.hokyozu.kyofuse.profiles.dto.response.GamerProfileResponse;
import com.hokyozu.kyofuse.profiles.service.GamerProfileService;
import com.hokyozu.kyofuse.teams.dto.request.TeamFilter;
import com.hokyozu.kyofuse.teams.dto.response.TeamResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    private final com.hokyozu.kyofuse.profiles.service.ProfileAnalyticsService profileAnalyticsService;

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

    @GetMapping("/me/analytics")
    public com.hokyozu.kyofuse.profiles.dto.response.ProfileAnalyticsResponse myAnalytics(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return profileAnalyticsService.getAnalytics(userId);
    }

    @GetMapping("/{username}")
    public GamerProfileResponse userProfile(@AuthenticationPrincipal Jwt jwt,
                                            @PathVariable String username) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return gamerProfileService.viewUserProfile(username, userId);
    }

    @GetMapping
    public Page<GamerProfileResponse> listingProfiles(@Valid @ModelAttribute ProfileFilter filter,
                                                   @AuthenticationPrincipal Jwt jwt,
                                                   @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UUID viewerId = jwt != null && jwt.getSubject() != null ? UUID.fromString(jwt.getSubject()) : null;
        return gamerProfileService.listingProfiles(filter, viewerId, pageable);
    }
}
