package com.hokyozu.kyofuse.reactions.service;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.finder.PostFinder;
import com.hokyozu.kyofuse.posts.repository.PostRepository;
import com.hokyozu.kyofuse.reactions.dto.request.PostReactionRequest;
import com.hokyozu.kyofuse.reactions.dto.response.PostReactionResponse;
import com.hokyozu.kyofuse.reactions.entity.PostReaction;
import com.hokyozu.kyofuse.reactions.enums.ReactionType;
import com.hokyozu.kyofuse.reactions.repository.PostReactionRepository;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostReactionServiceTest {

    @Mock
    private PostReactionRepository postReactionRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private PostFinder postFinder;
    @Mock
    private UserFinder userFinder;
    @InjectMocks
    private PostReactionService service;

    private UUID userId;
    private UUID postId;
    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
        user = User.builder().id(userId).username("player").build();
        post = Post.builder()
                .id(postId)
                .status(PostStatus.ACTIVE)
                .reactionCount(4)
                .likeCount(3)
                .build();
        when(postFinder.findById(postId)).thenReturn(post);
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
    }

    @Test
    void rejectsReactionToInactivePost() {
        post.setStatus(PostStatus.DELETED);
        when(postReactionRepository.findByPostIdAndUserId(postId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upsertReaction(userId, postId, request(ReactionType.LIKE)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Post não existente");

        verify(postRepository, never()).save(any());
        verify(postReactionRepository, never()).save(any());
    }

    @Test
    void createsLikeAndIncrementsLikeCount() {
        when(postReactionRepository.findByPostIdAndUserId(postId, userId)).thenReturn(Optional.empty());
        when(postReactionRepository.save(any(PostReaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PostReactionResponse response = service.upsertReaction(userId, postId, request(ReactionType.LIKE));

        assertThat(post.getLikeCount()).isEqualTo(4);
        assertThat(post.getReactionCount()).isEqualTo(4);
        assertThat(response).isEqualTo(new PostReactionResponse(postId, "player", ReactionType.LIKE));
        verify(postRepository).save(post);
    }

    @Test
    void createsNonLikeAndIncrementsReactionCount() {
        when(postReactionRepository.findByPostIdAndUserId(postId, userId)).thenReturn(Optional.empty());
        when(postReactionRepository.save(any(PostReaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PostReactionResponse response = service.upsertReaction(userId, postId, request(ReactionType.FIRE));

        assertThat(post.getLikeCount()).isEqualTo(3);
        assertThat(post.getReactionCount()).isEqualTo(5);
        assertThat(response.reactionType()).isEqualTo(ReactionType.FIRE);
    }

    @Test
    void changesNonLikeToLikeAndMovesCounter() {
        PostReaction existing = reaction(ReactionType.FIRE);
        stubExisting(existing);

        service.upsertReaction(userId, postId, request(ReactionType.LIKE));

        assertThat(post.getReactionCount()).isEqualTo(3);
        assertThat(post.getLikeCount()).isEqualTo(4);
        assertThat(existing.getReactionType()).isEqualTo(ReactionType.LIKE);
        assertThat(existing.getUpdatedAt()).isAfter(Instant.EPOCH);
    }

    @Test
    void changesLikeToNonLikeAndMovesCounter() {
        PostReaction existing = reaction(ReactionType.LIKE);
        stubExisting(existing);

        service.upsertReaction(userId, postId, request(ReactionType.CLUTCH));

        assertThat(post.getLikeCount()).isEqualTo(2);
        assertThat(post.getReactionCount()).isEqualTo(5);
        assertThat(existing.getReactionType()).isEqualTo(ReactionType.CLUTCH);
    }

    @Test
    void keepsCountersWhenLikeDoesNotChange() {
        PostReaction existing = reaction(ReactionType.LIKE);
        stubExisting(existing);

        service.upsertReaction(userId, postId, request(ReactionType.LIKE));

        assertThat(post.getLikeCount()).isEqualTo(3);
        assertThat(post.getReactionCount()).isEqualTo(4);
    }

    @Test
    void keepsCountersWhenChangingBetweenNonLikes() {
        PostReaction existing = reaction(ReactionType.FIRE);
        stubExisting(existing);

        service.upsertReaction(userId, postId, request(ReactionType.LOL));

        assertThat(post.getLikeCount()).isEqualTo(3);
        assertThat(post.getReactionCount()).isEqualTo(4);
        assertThat(existing.getReactionType()).isEqualTo(ReactionType.LOL);
    }

    private void stubExisting(PostReaction reaction) {
        when(postReactionRepository.findByPostIdAndUserId(postId, userId))
                .thenReturn(Optional.of(reaction));
        when(postReactionRepository.save(reaction)).thenReturn(reaction);
    }

    private PostReaction reaction(ReactionType type) {
        return PostReaction.builder()
                .post(post)
                .user(user)
                .reactionType(type)
                .createdAt(Instant.EPOCH)
                .updatedAt(Instant.EPOCH)
                .build();
    }

    private PostReactionRequest request(ReactionType type) {
        return new PostReactionRequest(type);
    }
}
