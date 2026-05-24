package com.hokyozu.kyofuse.posts.controller;

import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.dto.response.PostResponse;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostType;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.posts.service.PostService;
import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.executable.ExecutableValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController controller;

    private final ExecutableValidator executableValidator = Validation.buildDefaultValidatorFactory()
            .getValidator()
            .forExecutables();

    @Test
    void postUsesAuthenticatedUserId() {
        UUID userId = UUID.randomUUID();
        CreatePostRequest request = new CreatePostRequest(
                "content",
                PostType.TEXT,
                PostVisibility.PUBLIC,
                List.of(Cs2Map.MIRAGE)
        );
        PostResponse expected = response(UUID.randomUUID(), userId);
        when(postService.post(userId, request)).thenReturn(expected);

        PostResponse result = controller.post(jwt(userId), request);

        assertThat(result).isSameAs(expected);
        verify(postService).post(userId, request);
    }

    @Test
    void getPostUsesAuthenticatedUserIdAndPathPostId() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        PostResponse expected = response(postId, userId);
        when(postService.getPost(userId, postId)).thenReturn(expected);

        PostResponse result = controller.getPost(jwt(userId), postId);

        assertThat(result).isSameAs(expected);
        verify(postService).getPost(userId, postId);
    }

    @Test
    void getFeedBuildsDefaultSortedPageable() {
        Page<PostResponse> expected = new PageImpl<>(List.of(response(UUID.randomUUID(), UUID.randomUUID())));
        when(postService.getFeed(any(Pageable.class))).thenReturn(expected);

        Page<PostResponse> result = controller.getFeed(2, 30);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        assertThat(result).isSameAs(expected);
        verify(postService).getFeed(pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(30);
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt"))
                .extracting(Sort.Order::getDirection)
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getProfilePostsUsesPathProfileIdAndPageable() {
        UUID profileId = UUID.randomUUID();
        Page<PostResponse> expected = new PageImpl<>(List.of(response(UUID.randomUUID(), UUID.randomUUID())));
        when(postService.getProfilePosts(eq(profileId), any(Pageable.class))).thenReturn(expected);

        Page<PostResponse> result = controller.getProfilePosts(profileId, 1, 10);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        assertThat(result).isSameAs(expected);
        verify(postService).getProfilePosts(eq(profileId), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    void getMyPostsUsesAuthenticatedUserIdAndPageable() {
        UUID userId = UUID.randomUUID();
        Page<PostResponse> expected = new PageImpl<>(List.of(response(UUID.randomUUID(), userId)));
        when(postService.getMyPosts(eq(userId), any(Pageable.class))).thenReturn(expected);

        Page<PostResponse> result = controller.getMyPosts(jwt(userId), 3, 5);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        assertThat(result).isSameAs(expected);
        verify(postService).getMyPosts(eq(userId), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(3);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    void deletePostUsesAuthenticatedUserIdAndPathPostId() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        controller.deletePost(jwt(userId), postId);

        verify(postService).deletePost(userId, postId);
    }

    @Test
    void paginationRejectsNegativePageAndOutOfRangeSize() throws NoSuchMethodException {
        Method method = PostController.class.getMethod("getFeed", int.class, int.class);

        Set<ConstraintViolation<PostController>> violations = executableValidator.validateParameters(
                controller,
                method,
                new Object[]{-1, 101}
        );

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("getFeed.page", "getFeed.size");
    }

    @Test
    void paginationRejectsZeroSize() throws NoSuchMethodException {
        Method method = PostController.class.getMethod("getFeed", int.class, int.class);

        Set<ConstraintViolation<PostController>> violations = executableValidator.validateParameters(
                controller,
                method,
                new Object[]{0, 0}
        );

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("getFeed.size");
    }

    private static Jwt jwt(UUID userId) {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    private static PostResponse response(UUID postId, UUID authorId) {
        return new PostResponse(
                postId,
                authorId,
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
    }
}
