package com.hokyozu.kyofuse.teams.controller;

import com.hokyozu.kyofuse.teams.dto.request.TeamMemberEditRequest;
import com.hokyozu.kyofuse.teams.dto.response.TeamMemberResponse;
import com.hokyozu.kyofuse.teams.service.TeamMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public TeamMemberResponse addMember(@PathVariable String teamId,
                                        @AuthenticationPrincipal Jwt jwt,
                                        @PathVariable UUID userInvitedId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return teamMemberService.addMember(teamId, userId, userInvitedId);
    }

    @PatchMapping("/{teamId}/edit/{userEditedId}")
    public TeamMemberResponse editMember(@PathVariable String teamId,
                                         @PathVariable UUID userEditedId,
                                         @AuthenticationPrincipal Jwt jwt,
                                         @Valid @RequestBody TeamMemberEditRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return teamMemberService.editMember(teamId, userEditedId, userId, request);
    }

    @GetMapping("/{teamId}/members")
    public Page<TeamMemberResponse> listMembers(@PathVariable String teamId,
                                                @AuthenticationPrincipal Jwt jwt,
                                                Pageable pageable) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return teamMemberService.listMembers(teamId, userId, pageable);
    }

    @GetMapping("/{teamId}/{teamMemberId}")
    public TeamMemberResponse detailMember(@PathVariable String teamId,
                                           @PathVariable UUID teamMemberId,
                                           @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return teamMemberService.detailMember(teamId, userId, teamMemberId);
    }

    @DeleteMapping("/{teamId}/{userRemovedId}/remove")
    public void removeMember(@PathVariable String teamId,
                             @PathVariable UUID userRemovedId,
                             @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        teamMemberService.removeMember(teamId, userId, userRemovedId);
    }

    @DeleteMapping("/{teamId}/leave")
    public void leaveTeam(@PathVariable String teamId,
                          @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        teamMemberService.leaveTeam(teamId, userId);
    }
}
