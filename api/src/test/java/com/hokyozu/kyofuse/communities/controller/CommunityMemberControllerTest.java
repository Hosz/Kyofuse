package com.hokyozu.kyofuse.communities.controller;

import com.hokyozu.kyofuse.communities.dto.response.CommunityMemberResponse;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberRole;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.service.CommunityMemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityMemberControllerTest {

    @Mock
    private CommunityMemberService communityMemberService;

    @InjectMocks
    private CommunityMemberController controller;

    @Test
    void joinCommunityUsesAuthenticatedUserIdAndPathCommunityId() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        CommunityMemberResponse expected = response(communityId, userId);
        when(communityMemberService.joinCommunity(userId, communityId)).thenReturn(expected);

        CommunityMemberResponse result = controller.joinCommunity(jwt(userId), communityId);

        assertThat(result).isSameAs(expected);
        verify(communityMemberService).joinCommunity(userId, communityId);
    }

    @Test
    void listCommunityMembersDelegatesCommunityIdUserIdAndPageable() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<CommunityMemberResponse> expected = new PageImpl<>(List.of(response(communityId, userId)));
        when(communityMemberService.listCommunityMembers(communityId, userId, pageable)).thenReturn(expected);

        Page<CommunityMemberResponse> result = controller.listCommunityMembers(jwt(userId), communityId, pageable);

        assertThat(result).isSameAs(expected);
        verify(communityMemberService).listCommunityMembers(communityId, userId, pageable);
    }

    @Test
    void leaveCommunityUsesAuthenticatedUserIdAndPathCommunityId() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();

        controller.leaveCommunity(jwt(userId), communityId);

        verify(communityMemberService).leaveCommunity(userId, communityId);
    }

    @Test
    void removeMemberUsesAuthenticatedUserIdAndPathIds() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        controller.removeMember(jwt(userId), communityId, memberId);

        verify(communityMemberService).removeMember(userId, communityId, memberId);
    }

    private CommunityMemberResponse response(UUID communityId, UUID userId) {
        Instant now = Instant.now();
        return new CommunityMemberResponse(
                UUID.randomUUID(),
                communityId,
                "Kyofuse CS2",
                "kyofuse-cs2",
                userId,
                "member",
                CommunityMemberRole.MEMBER,
                CommunityMemberStatus.ACTIVE,
                now,
                null,
                now,
                now
        );
    }

    private Jwt jwt(UUID userId) {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
