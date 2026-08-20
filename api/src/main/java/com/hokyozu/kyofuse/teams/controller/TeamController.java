package com.hokyozu.kyofuse.teams.controller;

import com.hokyozu.kyofuse.teams.dto.request.TeamFilter;
import com.hokyozu.kyofuse.teams.dto.request.TeamRequest;
import com.hokyozu.kyofuse.teams.dto.request.UpdateTeamRequest;
import com.hokyozu.kyofuse.teams.dto.request.UpdateTeamRequiredRolesRequest;
import com.hokyozu.kyofuse.profiles.dto.response.GamerProfileResponse;
import com.hokyozu.kyofuse.teams.dto.response.TeamResponse;
import com.hokyozu.kyofuse.teams.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping("/create")
    public TeamResponse createTeams(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid TeamRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return teamService.createTeams(request, userId);
    }

    @GetMapping("/{teamId}")
    public TeamResponse detailTeam(@PathVariable UUID teamId) {
        return teamService.detailTeam(teamId);
    }

    @GetMapping
    public Page<TeamResponse> listingTeams(@AuthenticationPrincipal Jwt jwt, @Valid @ModelAttribute TeamFilter filter, @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return teamService.listingTeams(filter, pageable);
    }

    @GetMapping("/{teamId}/looking-for-team")
    public Page<GamerProfileResponse> listPlayersLookingForTeam(@AuthenticationPrincipal Jwt jwt,
                                                                @PathVariable UUID teamId,
                                                                @PageableDefault(size = 20) Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return teamService.listPlayersLookingForTeam(userId, teamId, pageable);
    }

    @GetMapping("/my-teams")
    public Page<TeamResponse> listingMyTeams(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return teamService.listingMyTeams(userId, pageable);
    }

    @GetMapping("/user/{userId}")
    public Page<TeamResponse> listingUserTeams(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId, Pageable pageable) {
        UUID viewerId = UUID.fromString(jwt.getSubject());

        return teamService.listingUserTeams(viewerId, userId, pageable);
    }

    @PatchMapping("/edit/{teamId}")
    public TeamResponse editTeam(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID teamId, @RequestBody @Valid UpdateTeamRequest updateTeamRequest) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return teamService.editTeam(userId, teamId, updateTeamRequest);
    }

    @PatchMapping("/{teamId}/inactivate")
    public TeamResponse inactiveTeam(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID teamId) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return teamService.inactiveTeam(userId, teamId);
    }

    @PutMapping("/{teamId}/required-roles")
    public TeamResponse manageRequiredRoles(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID teamId, @RequestBody @Valid UpdateTeamRequiredRolesRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return teamService.manageRequiredRoles(userId, teamId, request);
    }
}
