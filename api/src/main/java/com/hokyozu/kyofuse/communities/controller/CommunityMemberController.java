package com.hokyozu.kyofuse.communities.controller;

import com.hokyozu.kyofuse.communities.dto.request.UpdateCommunityMemberRoleRequest;
import com.hokyozu.kyofuse.communities.dto.response.CommunityMemberResponse;
import com.hokyozu.kyofuse.communities.service.CommunityMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/communities/members")
@RequiredArgsConstructor
public class CommunityMemberController {

    private final CommunityMemberService communityMemberService;

    @PostMapping("/{communityId}/join")
    public CommunityMemberResponse joinCommunity(@AuthenticationPrincipal Jwt jwt,
                                                 @PathVariable String communityId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return communityMemberService.joinCommunity(userId, communityId);
    }

    @GetMapping("/{communityId}")
    public Page<CommunityMemberResponse> listCommunityMembers(@AuthenticationPrincipal Jwt jwt,
                                                              @PathVariable String communityId,
                                                              Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return communityMemberService.listCommunityMembers(communityId, userId, pageable);
    }

    @PatchMapping("/{communityId}/role/{memberId}")
    public CommunityMemberResponse updateMemberRole(@AuthenticationPrincipal Jwt jwt,
                                                    @PathVariable String communityId,
                                                    @PathVariable UUID memberId,
                                                    @Valid @RequestBody UpdateCommunityMemberRoleRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return communityMemberService.updateMemberRole(userId, communityId, memberId, request.role());
    }

    @DeleteMapping("/{communityId}/leave")
    public void leaveCommunity(@AuthenticationPrincipal Jwt jwt,
                               @PathVariable String communityId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        communityMemberService.leaveCommunity(userId, communityId);
    }

    @DeleteMapping("/{communityId}/remove/{memberId}")
    public void removeMember(@AuthenticationPrincipal Jwt jwt,
                             @PathVariable String communityId,
                             @PathVariable UUID memberId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        communityMemberService.removeMember(userId, communityId, memberId);
    }
}
