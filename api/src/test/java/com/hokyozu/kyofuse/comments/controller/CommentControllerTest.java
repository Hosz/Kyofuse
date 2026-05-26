package com.hokyozu.kyofuse.comments.controller;

import com.hokyozu.kyofuse.comments.dto.request.CreateCommentRequest;
import com.hokyozu.kyofuse.comments.dto.response.CommentResponse;
import com.hokyozu.kyofuse.comments.enums.CommentStatus;
import com.hokyozu.kyofuse.comments.service.CommentService;
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
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController controller;

    @Test
    void postCommentUsesAuthenticatedUserIdAndPathPostId() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        CreateCommentRequest request = new CreateCommentRequest("content");
        CommentResponse expected = response(UUID.randomUUID(), postId, userId);
        when(commentService.postComment(userId, postId, request)).thenReturn(expected);

        CommentResponse result = controller.postComment(jwt(userId), postId, request);

        assertThat(result).isSameAs(expected);
        verify(commentService).postComment(userId, postId, request);
    }

    private static Jwt jwt(UUID userId) {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    private static CommentResponse response(UUID commentId, UUID postId, UUID authorId) {
        return new CommentResponse(
                commentId,
                postId,
                authorId,
                "content",
                CommentStatus.ACTIVE,
                0,
                0,
                Instant.now(),
                Instant.now()
        );
    }
}
