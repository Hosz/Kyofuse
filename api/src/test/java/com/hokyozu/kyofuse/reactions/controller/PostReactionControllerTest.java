package com.hokyozu.kyofuse.reactions.controller;

import com.hokyozu.kyofuse.reactions.dto.request.PostReactionRequest;
import com.hokyozu.kyofuse.reactions.dto.response.PostReactionResponse;
import com.hokyozu.kyofuse.reactions.enums.ReactionType;
import com.hokyozu.kyofuse.reactions.service.PostReactionService;
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
class PostReactionControllerTest {

    @Mock
    private PostReactionService service;
    @InjectMocks
    private PostReactionController controller;

    @Test
    void upsertReactionUsesAuthenticatedUserAndPostId() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        PostReactionRequest request = new PostReactionRequest(ReactionType.LIKE);
        PostReactionResponse expected = new PostReactionResponse(postId, userId, "player", "PlayerNick", "avatar.png", ReactionType.LIKE);
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
        when(service.upsertReaction(userId, postId, request)).thenReturn(expected);

        PostReactionResponse result = controller.upsertReaction(postId, request, jwt);

        assertThat(result).isSameAs(expected);
        verify(service).upsertReaction(userId, postId, request);
    }

    @Test
    void removeReactionUsesAuthenticatedUserAndPostId() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        controller.removeReaction(postId, jwt);

        verify(service).removeReaction(userId, postId);
    }
}
