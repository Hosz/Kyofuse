package com.hokyozu.kyofuse.communities.controller;

import com.hokyozu.kyofuse.communities.dto.request.CommunityRequest;
import com.hokyozu.kyofuse.communities.dto.request.UpdateCommunityRequest;
import com.hokyozu.kyofuse.communities.dto.response.CommunityResponse;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.communities.service.CommunityService;
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
class CommunityControllerTest {

    @Mock
    private CommunityService communityService;

    @InjectMocks
    private CommunityController controller;

    @Test
    void createCommunityUsesAuthenticatedUserId() {
        UUID userId = UUID.randomUUID();
        CommunityRequest request = request();
        CommunityResponse expected = response();
        when(communityService.createCommunity(request, userId)).thenReturn(expected);

        CommunityResponse result = controller.createCommunity(jwt(userId), request);

        assertThat(result).isSameAs(expected);
        verify(communityService).createCommunity(request, userId);
    }

    @Test
    void editCommunityUsesAuthenticatedUserIdAndPathCommunityId() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        UpdateCommunityRequest request = new UpdateCommunityRequest("Kyofuse CS2", "kyofuse-cs2", "Community description", null, null, CommunityVisibility.PUBLIC);
        CommunityResponse expected = response();
        when(communityService.editCommunity(userId, communityId, request)).thenReturn(expected);

        CommunityResponse result = controller.editCommunity(jwt(userId), communityId, request);

        assertThat(result).isSameAs(expected);
        verify(communityService).editCommunity(userId, communityId, request);
    }

    @Test
    void detailCommunityUsesPathCommunityId() {
        String communityId = "kyofuse-cs2";
        CommunityResponse expected = response();
        when(communityService.detailCommunity(communityId)).thenReturn(expected);

        CommunityResponse result = controller.detailCommunity(communityId);

        assertThat(result).isSameAs(expected);
        verify(communityService).detailCommunity(communityId);
    }

    @Test
    void deleteCommunityUsesAuthenticatedUserIdAndPathCommunityId() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();

        controller.deleteCommunity(jwt(userId), communityId);

        verify(communityService).deleteCommunity(userId, communityId);
    }

    @Test
    void archiveCommunityUsesAuthenticatedUserIdAndPathCommunityId() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();

        controller.archiveCommunity(jwt(userId), communityId);

        verify(communityService).archiveCommunity(userId, communityId);
    }

    @Test
    void listCommunitiesUsesAuthenticatedUserIdAndPageable() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<CommunityResponse> expected = new PageImpl<>(List.of(response()));
        when(communityService.listCommunities(userId, "kyofuse", pageable)).thenReturn(expected);

        Page<CommunityResponse> result = controller.listCommunities(jwt(userId), "kyofuse", pageable);

        assertThat(result).isSameAs(expected);
        verify(communityService).listCommunities(userId, "kyofuse", pageable);
    }

    @Test
    void listMyCommunitiesUsesAuthenticatedUserIdAndPageable() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<CommunityResponse> expected = new PageImpl<>(List.of(response()));
        when(communityService.listMyCommunities(userId, pageable)).thenReturn(expected);

        Page<CommunityResponse> result = controller.listMyCommunities(jwt(userId), pageable);

        assertThat(result).isSameAs(expected);
        verify(communityService).listMyCommunities(userId, pageable);
    }

    private CommunityRequest request() {
        return new CommunityRequest(
                "Kyofuse CS2",
                "kyofuse-cs2",
                "Community description",
                null,
                null,
                CommunityVisibility.PUBLIC
        );
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
