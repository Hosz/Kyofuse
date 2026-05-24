package com.hokyozu.kyofuse.posts.service;

import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.dto.response.PostResponse;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.entity.PostMap;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostType;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.posts.repository.PostMapRepository;
import com.hokyozu.kyofuse.posts.repository.PostRepository;
import com.hokyozu.kyofuse.posts.validator.DeletePostValidator;
import com.hokyozu.kyofuse.posts.validator.PostMapsValidator;
import com.hokyozu.kyofuse.posts.validator.PostValidator;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private GamerProfileRepository gamerProfileRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapRepository postMapRepository;

    @Mock
    private PostValidator postValidator;

    @Mock
    private PostMapsValidator postMapsValidator;

    @Mock
    private DeletePostValidator deletePostValidator;

    @InjectMocks
    private PostService postService;

    @Test
    void postCreatesActivePostForAuthenticatedUserProfile() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        User user = User.builder().id(userId).username("player").build();
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .build();
        CreatePostRequest request = new CreatePostRequest(
                "hello world",
                PostType.TEXT,
                PostVisibility.PUBLIC,
                List.of(Cs2Map.MIRAGE, Cs2Map.INFERNO)
        );

        when(gamerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(postId);
            return post;
        });

        PostResponse response = postService.post(userId, request);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postValidator).validate(request);
        verify(postMapsValidator).validate(request);
        verify(postRepository).save(postCaptor.capture());
        ArgumentCaptor<List<PostMap>> postMapsCaptor = postMapsCaptor();
        verify(postMapRepository).saveAll(postMapsCaptor.capture());

        Post savedPost = postCaptor.getValue();
        assertThat(savedPost.getAuthor()).isSameAs(user);
        assertThat(savedPost.getContent()).isEqualTo("hello world");
        assertThat(savedPost.getPostType()).isEqualTo(PostType.TEXT);
        assertThat(savedPost.getVisibility()).isEqualTo(PostVisibility.PUBLIC);
        assertThat(savedPost.getStatus()).isEqualTo(PostStatus.ACTIVE);
        assertThat(savedPost.getCreatedAt()).isNotNull();
        assertThat(savedPost.getUpdatedAt()).isNotNull();
        assertThat(postMapsCaptor.getValue())
                .extracting(PostMap::getMapName)
                .containsExactly("MIRAGE", "INFERNO");
        assertThat(postMapsCaptor.getValue())
                .allSatisfy(postMap -> assertThat(postMap.getPost()).isSameAs(savedPost));

        assertThat(response.id()).isEqualTo(postId);
        assertThat(response.authorId()).isEqualTo(userId);
        assertThat(response.content()).isEqualTo("hello world");
        assertThat(response.postStatus()).isEqualTo(PostStatus.ACTIVE);
        assertThat(response.maps()).containsExactly("MIRAGE", "INFERNO");
    }

    @Test
    void postDoesNotSaveMapsWhenRequestMapsIsNull() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        User user = User.builder().id(userId).username("player").build();
        GamerProfile profile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .build();
        CreatePostRequest request = new CreatePostRequest(
                "hello world",
                PostType.TEXT,
                PostVisibility.PUBLIC,
                null
        );

        when(gamerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(postId);
            return post;
        });

        PostResponse response = postService.post(userId, request);

        verify(postValidator).validate(request);
        verify(postMapsValidator).validate(request);
        verify(postMapRepository, never()).saveAll(any());
        assertThat(response.maps()).isEmpty();
    }

    @Test
    void postThrowsWhenUserProfileDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(gamerProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.post(
                userId,
                new CreatePostRequest("content", PostType.TEXT, PostVisibility.PUBLIC, null)
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Gamer profile not found for user ID: " + userId);
    }

    @Test
    void getPostReturnsVisiblePostForRequesterAndLoadsMaps() {
        UUID requesterId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Post post = post(postId, requesterId, PostVisibility.PRIVATE, PostStatus.ACTIVE);
        List<PostMap> postMaps = List.of(postMap(post, "MIRAGE"));
        when(postRepository.findVisiblePostForUser(postId, requesterId, PostStatus.DELETED, PostVisibility.PUBLIC))
                .thenReturn(Optional.of(post));
        when(postMapRepository.findByPostId(postId)).thenReturn(postMaps);

        PostResponse response = postService.getPost(requesterId, postId);

        verify(postRepository).findVisiblePostForUser(postId, requesterId, PostStatus.DELETED, PostVisibility.PUBLIC);
        verify(postMapRepository).findByPostId(postId);
        assertThat(response.id()).isEqualTo(postId);
        assertThat(response.authorId()).isEqualTo(requesterId);
        assertThat(response.maps()).containsExactly("MIRAGE");
    }

    @Test
    void getPostThrowsBadRequestWhenPostIsNotVisibleForRequester() {
        UUID requesterId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        when(postRepository.findVisiblePostForUser(postId, requesterId, PostStatus.DELETED, PostVisibility.PUBLIC))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPost(requesterId, postId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Post not found for ID: " + postId);
        verify(postMapRepository, never()).findByPostId(any());
    }

    @Test
    void getFeedReturnsOnlyPublicActivePostsAndMapsResponses() {
        Pageable pageable = PageRequest.of(0, 20);
        Post firstPost = post(UUID.randomUUID(), UUID.randomUUID(), PostVisibility.PUBLIC, PostStatus.ACTIVE);
        Post secondPost = post(UUID.randomUUID(), UUID.randomUUID(), PostVisibility.PUBLIC, PostStatus.ACTIVE);
        when(postRepository.findByVisibilityAndStatus(PostVisibility.PUBLIC, PostStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(firstPost, secondPost), pageable, 2));
        when(postMapRepository.findByPostId(firstPost.getId())).thenReturn(List.of(postMap(firstPost, "MIRAGE")));
        when(postMapRepository.findByPostId(secondPost.getId())).thenReturn(List.of(postMap(secondPost, "INFERNO")));

        Page<PostResponse> response = postService.getFeed(pageable);

        verify(postRepository).findByVisibilityAndStatus(PostVisibility.PUBLIC, PostStatus.ACTIVE, pageable);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent()).extracting(PostResponse::id)
                .containsExactly(firstPost.getId(), secondPost.getId());
        assertThat(response.getContent()).extracting(PostResponse::maps)
                .containsExactly(List.of("MIRAGE"), List.of("INFERNO"));
    }

    @Test
    void getProfilePostsReturnsOnlyPublicActivePostsForAuthor() {
        UUID authorId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(1, 10);
        Post post = post(UUID.randomUUID(), authorId, PostVisibility.PUBLIC, PostStatus.ACTIVE);
        when(postRepository.findByAuthorIdAndVisibilityInAndStatus(
                authorId,
                List.of(PostVisibility.PUBLIC),
                PostStatus.ACTIVE,
                pageable
        )).thenReturn(new PageImpl<>(List.of(post), pageable, 1));
        when(postMapRepository.findByPostId(post.getId())).thenReturn(List.of(postMap(post, "NUKE")));

        Page<PostResponse> response = postService.getProfilePosts(authorId, pageable);

        verify(postRepository).findByAuthorIdAndVisibilityInAndStatus(
                authorId,
                List.of(PostVisibility.PUBLIC),
                PostStatus.ACTIVE,
                pageable
        );
        assertThat(response.getContent()).singleElement()
                .satisfies(postResponse -> {
                    assertThat(postResponse.id()).isEqualTo(post.getId());
                    assertThat(postResponse.maps()).containsExactly("NUKE");
                });
    }

    @Test
    void getMyPostsIncludesOwnPublicPrivateActiveAndHiddenPosts() {
        UUID authorId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 5);
        Post hiddenPost = post(UUID.randomUUID(), authorId, PostVisibility.PRIVATE, PostStatus.HIDDEN);
        when(postRepository.findByAuthorIdAndVisibilityInAndStatusIn(
                eq(authorId),
                anyList(),
                anyList(),
                eq(pageable)
        )).thenReturn(new PageImpl<>(List.of(hiddenPost), pageable, 1));
        when(postMapRepository.findByPostId(hiddenPost.getId())).thenReturn(List.of());

        Page<PostResponse> response = postService.getMyPosts(authorId, pageable);

        ArgumentCaptor<List<PostVisibility>> visibilityCaptor = visibilityListCaptor();
        ArgumentCaptor<List<PostStatus>> statusCaptor = statusListCaptor();
        verify(postRepository).findByAuthorIdAndVisibilityInAndStatusIn(
                eq(authorId),
                visibilityCaptor.capture(),
                statusCaptor.capture(),
                eq(pageable)
        );
        assertThat(visibilityCaptor.getValue()).containsExactly(PostVisibility.PUBLIC, PostVisibility.PRIVATE);
        assertThat(statusCaptor.getValue()).containsExactly(PostStatus.ACTIVE, PostStatus.HIDDEN);
        assertThat(response.getContent()).singleElement()
                .extracting(PostResponse::postStatus)
                .isEqualTo(PostStatus.HIDDEN);
    }

    @Test
    void deletePostMarksPostAsDeletedAfterValidation() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Post post = post(postId, userId, PostVisibility.PUBLIC, PostStatus.ACTIVE);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.deletePost(userId, postId);

        verify(deletePostValidator).validate(post, userId);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue()).isSameAs(post);
        assertThat(post.getStatus()).isEqualTo(PostStatus.DELETED);
    }

    @Test
    void deletePostThrowsBadRequestWhenPostDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.deletePost(userId, postId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Post not found for ID: " + postId);

        verify(deletePostValidator, never()).validate(any(), any());
        verify(postRepository, never()).save(any());
    }

    private static Post post(UUID postId, UUID authorId, PostVisibility visibility, PostStatus status) {
        return Post.builder()
                .id(postId)
                .author(User.builder().id(authorId).username("player").build())
                .content("content")
                .postType(PostType.TEXT)
                .visibility(visibility)
                .status(status)
                .reactionCount(0)
                .likeCount(0)
                .commentCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private static PostMap postMap(Post post, String mapName) {
        return PostMap.builder()
                .id(UUID.randomUUID())
                .post(post)
                .mapName(mapName)
                .createdAt(Instant.now())
                .build();
    }

    @SuppressWarnings("unchecked")
    private static ArgumentCaptor<List<PostMap>> postMapsCaptor() {
        return ArgumentCaptor.forClass(List.class);
    }

    @SuppressWarnings("unchecked")
    private static ArgumentCaptor<List<PostVisibility>> visibilityListCaptor() {
        return ArgumentCaptor.forClass(List.class);
    }

    @SuppressWarnings("unchecked")
    private static ArgumentCaptor<List<PostStatus>> statusListCaptor() {
        return ArgumentCaptor.forClass(List.class);
    }
}
