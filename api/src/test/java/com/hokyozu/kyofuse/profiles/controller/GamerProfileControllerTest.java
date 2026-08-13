package com.hokyozu.kyofuse.profiles.controller;

import com.hokyozu.kyofuse.profiles.dto.request.GamerProfileRequest;
import com.hokyozu.kyofuse.profiles.dto.response.GamerProfileResponse;
import com.hokyozu.kyofuse.profiles.enums.GamerProfileSetupStatus;
import com.hokyozu.kyofuse.profiles.service.GamerProfileService;
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
class GamerProfileControllerTest {

    @Mock
    private GamerProfileService gamerProfileService;

    @InjectMocks
    private GamerProfileController controller;

    @Test
    void editProfileUsesAuthenticatedUserId() {
        UUID userId = UUID.randomUUID();
        GamerProfileRequest request = emptyRequest("newNick");
        GamerProfileResponse expected = response(userId);
        when(gamerProfileService.editProfile(userId, request)).thenReturn(expected);

        GamerProfileResponse result = controller.editProfile(jwt(userId), request);

        assertThat(result).isSameAs(expected);
        verify(gamerProfileService).editProfile(userId, request);
    }

    @Test
    void myProfileUsesAuthenticatedUserId() {
        UUID userId = UUID.randomUUID();
        GamerProfileResponse expected = response(userId);
        when(gamerProfileService.viewMyProfile(userId)).thenReturn(expected);

        GamerProfileResponse result = controller.myProfile(jwt(userId));

        assertThat(result).isSameAs(expected);
        verify(gamerProfileService).viewMyProfile(userId);
    }

    @Test
    void userProfileUsesPathProfileId() {
        UUID profileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GamerProfileResponse expected = response(UUID.randomUUID());
        when(gamerProfileService.viewUserProfile(profileId, userId)).thenReturn(expected);

        GamerProfileResponse result = controller.userProfile(jwt(userId), profileId);

        assertThat(result).isSameAs(expected);
        verify(gamerProfileService).viewUserProfile(profileId, userId);
    }

    private static Jwt jwt(UUID userId) {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    private static GamerProfileRequest emptyRequest(String nickname) {
        return new GamerProfileRequest(
                nickname,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private static GamerProfileResponse response(UUID userId) {
        return new GamerProfileResponse(
                UUID.randomUUID(),
                userId,
                "player",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                GamerProfileSetupStatus.PENDING,
                List.of(),
                Instant.now(),
                Instant.now()
        );
    }
}
