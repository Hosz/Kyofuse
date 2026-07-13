package com.hokyozu.kyofuse.teams.controller;

import com.hokyozu.kyofuse.teams.dto.request.TeamMemberEditRequest;
import com.hokyozu.kyofuse.teams.dto.response.TeamMemberResponse;
import com.hokyozu.kyofuse.teams.service.TeamMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/team-member/")
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    @PostMapping("/{teamId}/add/{userInvitedId}")
    public TeamMemberResponse addMember(@PathVariable UUID teamId, @AuthenticationPrincipal Jwt jwt, @PathVariable UUID userInvitedId) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return teamMemberService.addMember(teamId, userId, userInvitedId);
    }

    @PatchMapping("/{teamId}/edit/{userEditedId}")
    public TeamMemberResponse editMember(@PathVariable UUID teamId,
                                         @PathVariable UUID userEditedId,
                                         @AuthenticationPrincipal Jwt jwt,
                                         @Valid @RequestBody TeamMemberEditRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());

        return teamMemberService.editMember(teamId, userId, userEditedId, request);
    }
}
