package com.hokyozu.kyofuse.posts.controller;

import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.dto.response.PostResponse;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostType;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.posts.service.PostService;
import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
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
class PostControllerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController controller;

    @Test
    void postUsesAuthenticatedUserId() {
        UUID userId = UUID.randomUUID();
        CreatePostRequest request = new CreatePostRequest(
                "content",
                PostType.TEXT,
                PostVisibility.PUBLIC,
                List.of(Cs2Map.MIRAGE)
        );
        PostResponse expected = new PostResponse(
                UUID.randomUUID(),
                userId,
                "content",
                PostType.TEXT,
                PostVisibility.PUBLIC,
                PostStatus.ACTIVE,
                0,
                0,
                0,
                List.of("MIRAGE"),
                Instant.now(),
                Instant.now()
        );
        when(postService.post(userId, request)).thenReturn(expected);

        PostResponse result = controller.post(jwt(userId), request);

        assertThat(result).isSameAs(expected);
        verify(postService).post(userId, request);
    }

    private static Jwt jwt(UUID userId) {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
