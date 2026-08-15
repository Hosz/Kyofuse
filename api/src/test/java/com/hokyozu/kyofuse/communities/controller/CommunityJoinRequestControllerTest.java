package com.hokyozu.kyofuse.communities.controller;

import com.hokyozu.kyofuse.communities.dto.response.CommunityJoinRequestResponse;
import com.hokyozu.kyofuse.communities.service.CommunityJoinRequestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityJoinRequestControllerTest {

    @Mock
    private CommunityJoinRequestService communityJoinRequestService;

    @InjectMocks
    private CommunityJoinRequestController controller;

    @Test
    void requestToJoinCommunityUsesAuthenticatedUserIdAndPathCommunityId() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        CommunityJoinRequestResponse expected = response(communityId, userId);
        when(communityJoinRequestService.requestToJoinCommunity(userId, communityId)).thenReturn(expected);

        CommunityJoinRequestResponse result = controller.requestToJoinCommunity(jwt(userId), communityId);

        assertThat(result).isSameAs(expected);
        verify(communityJoinRequestService).requestToJoinCommunity(userId, communityId);
    }

    @Test
    void approveJoinRequestUsesAuthenticatedUserIdAndPathRequestId() {
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        controller.approveJoinRequest(jwt(userId), requestId);

        verify(communityJoinRequestService).approveJoinRequest(userId, requestId);
    }

    @Test
    void rejectJoinRequestUsesAuthenticatedUserIdAndPathRequestId() {
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        controller.rejectJoinRequest(jwt(userId), requestId);

        verify(communityJoinRequestService).rejectJoinRequest(userId, requestId);
    }

    private CommunityJoinRequestResponse response(UUID communityId, UUID userId) {
        return new CommunityJoinRequestResponse(
                UUID.randomUUID(),
                communityId,
                "Kyofuse CS2",
                "kyofuse-cs2",
                userId,
                "requester",
                Instant.now()
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
