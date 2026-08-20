package com.hokyozu.kyofuse.reactions.controller;

import com.hokyozu.kyofuse.reactions.dto.request.CommentReactionRequest;
import com.hokyozu.kyofuse.reactions.dto.response.CommentReactionResponse;
import com.hokyozu.kyofuse.reactions.enums.ReactionType;
import com.hokyozu.kyofuse.reactions.service.CommentReactionService;
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
class CommentReactionControllerTest {

    @Mock
    private CommentReactionService service;
    @InjectMocks
    private CommentReactionController controller;

    @Test
    void upsertReactionForwardsPathAndAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        CommentReactionRequest request = new CommentReactionRequest(ReactionType.LIKE);
        CommentReactionResponse expected =
                new CommentReactionResponse(postId, commentId, userId, "player", "PlayerNick", "avatar.png", ReactionType.LIKE);
        when(service.upsertReaction(postId, commentId, request, userId)).thenReturn(expected);

        CommentReactionResponse result =
                controller.upsertReaction(postId, commentId, request, jwt(userId));

        assertThat(result).isSameAs(expected);
        verify(service).upsertReaction(postId, commentId, request, userId);
    }

    @Test
    void removeReactionForwardsPathAndAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        controller.removeReaction(postId, commentId, jwt(userId));

        verify(service).removeReaction(userId, postId, commentId);
    }

    private Jwt jwt(UUID userId) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
    }
}
