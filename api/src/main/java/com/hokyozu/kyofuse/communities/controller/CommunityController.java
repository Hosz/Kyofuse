package com.hokyozu.kyofuse.communities.controller;

import com.hokyozu.kyofuse.communities.dto.request.CommunityRequest;
import com.hokyozu.kyofuse.communities.dto.request.UpdateCommunityRequest;
import com.hokyozu.kyofuse.communities.dto.response.CommunityResponse;
import com.hokyozu.kyofuse.communities.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.hokyozu.kyofuse.teams.dto.response.TeamResponse;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping("/create")
    public CommunityResponse createCommunity(@AuthenticationPrincipal Jwt jwt,
                                             @RequestBody @Valid CommunityRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return communityService.createCommunity(request, userId);
    }

    @PostMapping(value = "/{communityId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommunityResponse uploadAvatar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID communityId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        UUID userId = UUID.fromString(jwt.getSubject());
        return communityService.uploadAvatar(userId, communityId, file);
    }

    @PostMapping(value = "/{communityId}/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommunityResponse uploadBanner(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID communityId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        UUID userId = UUID.fromString(jwt.getSubject());
        return communityService.uploadBanner(userId, communityId, file);
    }

    @PatchMapping("/edit/{communityId}")
    public CommunityResponse editCommunity(@AuthenticationPrincipal Jwt jwt,
                                           @PathVariable UUID communityId,
                                           @RequestBody @Valid UpdateCommunityRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return communityService.editCommunity(userId, communityId, request);
    }

    @GetMapping("/{communityId}")
    public CommunityResponse detailCommunity(@PathVariable String communityId) {
        return communityService.detailCommunity(communityId);
    }

    @DeleteMapping("/{communityId}")
    public void deleteCommunity(@AuthenticationPrincipal Jwt jwt,
                                @PathVariable UUID communityId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        communityService.deleteCommunity(userId, communityId);
    }

    @PatchMapping("/{communityId}/archive")
    public void archiveCommunity(@AuthenticationPrincipal Jwt jwt,
                                 @PathVariable UUID communityId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        communityService.archiveCommunity(userId, communityId);
    }

    @GetMapping("/list-communities")
    public Page<CommunityResponse> listCommunities(@AuthenticationPrincipal Jwt jwt,
                                                   @RequestParam(required = false) String name,
                                                   Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return communityService.listCommunities(userId, name, pageable);
    }

    @GetMapping("/by-team/{teamId}")
    public CommunityResponse detailCommunityByTeam(@PathVariable String teamId) {
        return communityService.detailCommunityByTeam(teamId);
    }

    @GetMapping("/user/{userId}")
    public Page<CommunityResponse> listUserCommunities(@AuthenticationPrincipal Jwt jwt,
                                                       @PathVariable UUID userId,
                                                       Pageable pageable) {
        UUID viewerId = UUID.fromString(jwt.getSubject());
        return communityService.listUserCommunities(viewerId, userId, pageable);
    }

    @GetMapping("/my-communities")
    public Page<CommunityResponse> listMyCommunities(@AuthenticationPrincipal Jwt jwt,
                                                      Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return communityService.listMyCommunities(userId, pageable);
    }

    @PostMapping("/{communityId}/attach-team/{teamId}")
    public CommunityResponse attachTeamToCommunity(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String communityId,
            @PathVariable String teamId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return communityService.attachTeamAndCommunity(userId, teamId, communityId);
    }

    @GetMapping("/{communityId}/available-teams")
    public List<TeamResponse> listAvailableTeamsForCommunity(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String communityId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return communityService.listAvailableTeamsForCommunity(userId, communityId);
    }
}
