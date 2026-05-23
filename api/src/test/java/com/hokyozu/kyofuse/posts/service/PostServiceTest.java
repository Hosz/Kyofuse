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
import com.hokyozu.kyofuse.posts.validator.PostValidator;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
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
        verify(postRepository).save(postCaptor.capture());
        ArgumentCaptor<List<PostMap>> postMapsCaptor = ArgumentCaptor.forClass(List.class);
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
}
