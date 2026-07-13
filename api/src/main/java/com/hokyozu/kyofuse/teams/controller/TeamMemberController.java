package com.hokyozu.kyofuse.teams.controller;

import com.hokyozu.kyofuse.teams.dto.response.TeamMemberResponse;
import com.hokyozu.kyofuse.teams.service.TeamMemberService;
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
}
