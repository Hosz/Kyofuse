package com.hokyozu.kyofuse.communities.controller;

import com.hokyozu.kyofuse.communities.dto.request.CommunityInviteCancelRequest;
import com.hokyozu.kyofuse.communities.dto.request.CommunityInviteRequest;
import com.hokyozu.kyofuse.communities.dto.response.CommunityInviteResponse;
import com.hokyozu.kyofuse.communities.enums.CommunityInviteStatus;
import com.hokyozu.kyofuse.communities.service.CommunityInviteService;
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
class CommunityInviteControllerTest {

    @Mock
    private CommunityInviteService communityInviteService;

    @InjectMocks
    private CommunityInviteController controller;

    @Test
    void inviteUserDelegatesToService() {
        UUID userId = UUID.randomUUID();
        String communityIdentifier = "comm-slug";
        String receiverUsername = "testuser";
        CommunityInviteRequest request = new CommunityInviteRequest("Welcome!");
        CommunityInviteResponse expected = response(UUID.randomUUID(), userId);

        when(communityInviteService.inviteUser(userId, communityIdentifier, receiverUsername, request)).thenReturn(expected);

        CommunityInviteResponse result = controller.inviteUser(communityIdentifier, receiverUsername, jwt(userId), request);

        assertThat(result).isSameAs(expected);
        verify(communityInviteService).inviteUser(userId, communityIdentifier, receiverUsername, request);
    }

    @Test
    void listInvitesDelegatesToService() {
        UUID userId = UUID.randomUUID();
        String communityIdentifier = "comm-slug";
        Pageable pageable = PageRequest.of(0, 10);
        Page<CommunityInviteResponse> expected = new PageImpl<>(List.of(response(UUID.randomUUID(), userId)));

        when(communityInviteService.listInvites(userId, communityIdentifier, pageable, CommunityInviteStatus.PENDING)).thenReturn(expected);

        Page<CommunityInviteResponse> result = controller.listInvites(communityIdentifier, jwt(userId), pageable, CommunityInviteStatus.PENDING);

        assertThat(result).isSameAs(expected);
        verify(communityInviteService).listInvites(userId, communityIdentifier, pageable, CommunityInviteStatus.PENDING);
    }

    @Test
    void acceptInviteDelegatesToService() {
        UUID userId = UUID.randomUUID();
        UUID inviteId = UUID.randomUUID();

        controller.acceptInvite(inviteId, jwt(userId));

        verify(communityInviteService).acceptInvite(userId, inviteId);
    }

    @Test
    void declineInviteDelegatesToService() {
        UUID userId = UUID.randomUUID();
        UUID inviteId = UUID.randomUUID();

        controller.declineInvite(inviteId, jwt(userId));

        verify(communityInviteService).declineInvite(userId, inviteId);
    }

    @Test
    void cancelInviteDelegatesToService() {
        UUID userId = UUID.randomUUID();
        UUID inviteId = UUID.randomUUID();
        CommunityInviteCancelRequest request = new CommunityInviteCancelRequest("Oops");

        controller.cancelInvite(inviteId, jwt(userId), request);

        verify(communityInviteService).cancelInvite(userId, inviteId, request);
    }

    private CommunityInviteResponse response(UUID id, UUID senderId) {
        return new CommunityInviteResponse(
                id,
                UUID.randomUUID(),
                "Community Name",
                "community-slug",
                null,
                senderId,
                "sender",
                UUID.randomUUID(),
                "receiver",
                CommunityInviteStatus.PENDING,
                "Hello",
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
