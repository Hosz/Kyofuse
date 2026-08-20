package com.hokyozu.kyofuse.communities.controller;

import com.hokyozu.kyofuse.communities.dto.response.CommunityResponse;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.communities.service.UserPinnedCommunityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPinnedCommunityControllerTest {

    @Mock
    private UserPinnedCommunityService userPinnedCommunityService;

    @InjectMocks
    private UserPinnedCommunityController controller;

    @Test
    void listPinnedCommunitiesUsesAuthenticatedUserId() {
        UUID userId = UUID.randomUUID();
        List<CommunityResponse> expected = List.of(response());
        when(userPinnedCommunityService.listPinnedCommunities(userId)).thenReturn(expected);

        List<CommunityResponse> result = controller.listPinnedCommunities(jwt(userId));

        assertThat(result).isSameAs(expected);
        verify(userPinnedCommunityService).listPinnedCommunities(userId);
    }

    @Test
    void pinCommunityUsesAuthenticatedUserIdAndPathCommunityId() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        CommunityResponse expected = response();
        when(userPinnedCommunityService.pinCommunity(userId, communityId)).thenReturn(expected);

        CommunityResponse result = controller.pinCommunity(jwt(userId), communityId);

        assertThat(result).isSameAs(expected);
        verify(userPinnedCommunityService).pinCommunity(userId, communityId);
    }

    @Test
    void unpinCommunityUsesAuthenticatedUserIdAndPathCommunityId() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();

        controller.unpinCommunity(jwt(userId), communityId);

        verify(userPinnedCommunityService).unpinCommunity(userId, communityId);
    }

    private CommunityResponse response() {
        Instant now = Instant.now();
        return new CommunityResponse(
                UUID.randomUUID(),
                "Kyofuse CS2",
                "kyofuse-cs2",
                "Community description",
                null,
                null,
                UUID.randomUUID(),
                "owner",
                null,
                null,
                null,
                CommunityVisibility.PUBLIC,
                CommunityStatus.ACTIVE,
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
