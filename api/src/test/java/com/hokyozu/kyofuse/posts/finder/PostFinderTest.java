package com.hokyozu.kyofuse.posts.finder;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.posts.repository.PostRepository;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostFinderTest {

    @Mock
    private PostRepository repository;
    @InjectMocks
    private PostFinder finder;

    @Test
    void findsPostByIdAndStatus() {
        UUID postId = UUID.randomUUID();
        Post post = Post.builder().id(postId).build();
        when(repository.findByIdAndStatus(postId, PostStatus.ACTIVE)).thenReturn(Optional.of(post));

        assertThat(finder.findPostByIdAndStatus(postId, PostStatus.ACTIVE)).isSameAs(post);
    }

    @Test
    void rejectsMissingPostByIdAndStatus() {
        UUID postId = UUID.randomUUID();
        when(repository.findByIdAndStatus(postId, PostStatus.ACTIVE)).thenReturn(Optional.empty());

        assertMissing(() -> finder.findPostByIdAndStatus(postId, PostStatus.ACTIVE), postId);
    }

    @Test
    void findsVisiblePostForUser() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Post post = Post.builder().id(postId).build();
        when(repository.findVisiblePostForUser(
                postId, userId, PostStatus.ACTIVE, PostVisibility.PUBLIC
        )).thenReturn(Optional.of(post));

        assertThat(finder.findVisiblePostForUser(
                postId, userId, PostStatus.ACTIVE, PostVisibility.PUBLIC
        )).isSameAs(post);
    }

    @Test
    void rejectsMissingVisiblePost() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(repository.findVisiblePostForUser(
                postId, userId, PostStatus.ACTIVE, PostVisibility.PUBLIC
        )).thenReturn(Optional.empty());

        assertMissing(
                () -> finder.findVisiblePostForUser(
                        postId, userId, PostStatus.ACTIVE, PostVisibility.PUBLIC
                ),
                postId
        );
    }

    @Test
    void findsPostById() {
        UUID postId = UUID.randomUUID();
        Post post = Post.builder().id(postId).build();
        when(repository.findById(postId)).thenReturn(Optional.of(post));

        assertThat(finder.findById(postId)).isSameAs(post);
    }

    @Test
    void rejectsMissingPostById() {
        UUID postId = UUID.randomUUID();
        when(repository.findById(postId)).thenReturn(Optional.empty());

        assertMissing(() -> finder.findById(postId), postId);
    }

    private void assertMissing(Runnable invocation, UUID postId) {
        assertThatThrownBy(invocation::run)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Post not found for ID: " + postId);
    }
}
