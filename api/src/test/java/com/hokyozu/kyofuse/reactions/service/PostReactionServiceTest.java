package com.hokyozu.kyofuse.reactions.service;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.posts.finder.PostFinder;
import com.hokyozu.kyofuse.posts.repository.PostRepository;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.reactions.dto.request.PostReactionRequest;
import com.hokyozu.kyofuse.reactions.dto.response.PostReactionResponse;
import com.hokyozu.kyofuse.reactions.entity.PostReaction;
import com.hokyozu.kyofuse.reactions.enums.ReactionType;
import com.hokyozu.kyofuse.reactions.repository.PostReactionRepository;
import com.hokyozu.kyofuse.relationships.permission.service.post.PostPermissionService;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
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
    @Mock
    private PostPermissionService postPermissionService;
    @Mock
    private GamerProfileFinder gamerProfileFinder;
    @Mock
    private UserChecker userChecker;
    @InjectMocks
    private PostReactionService service;

    private UUID userId;
    private UUID postId;
    private User user;
    private Post post;
    private GamerProfile profile;

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
        profile = GamerProfile.builder().nickname("PlayerNick").avatarUrl("avatar.png").build();
        lenient().when(postFinder.findVisiblePostForUser(
                postId, userId, PostStatus.ACTIVE, PostVisibility.PUBLIC
        )).thenReturn(post);
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        lenient().when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profile);
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
        assertThat(response).isEqualTo(new PostReactionResponse(postId, userId, "player", "PlayerNick", "avatar.png", ReactionType.LIKE));
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

    @Test
    void removesLikeAndDecrementsLikeCount() {
        PostReaction existing = reaction(ReactionType.LIKE);
        when(postFinder.findVisibleActivePost(postId, userId)).thenReturn(post);
        when(postReactionRepository.findByPostIdAndUserId(postId, userId))
                .thenReturn(Optional.of(existing));

        service.removeReaction(userId, postId);

        assertThat(post.getLikeCount()).isEqualTo(2);
        assertThat(post.getReactionCount()).isEqualTo(4);
        verify(postRepository).save(post);
        verify(postReactionRepository).delete(existing);
    }

    @Test
    void removesNonLikeAndDecrementsReactionCount() {
        PostReaction existing = reaction(ReactionType.FIRE);
        when(postFinder.findVisibleActivePost(postId, userId)).thenReturn(post);
        when(postReactionRepository.findByPostIdAndUserId(postId, userId))
                .thenReturn(Optional.of(existing));

        service.removeReaction(userId, postId);

        assertThat(post.getLikeCount()).isEqualTo(3);
        assertThat(post.getReactionCount()).isEqualTo(3);
        verify(postReactionRepository).delete(existing);
    }

    @Test
    void rejectsRemovalWhenReactionDoesNotExist() {
        when(postFinder.findVisibleActivePost(postId, userId)).thenReturn(post);
        when(postReactionRepository.findByPostIdAndUserId(postId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeReaction(userId, postId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Reação de post não encontrada.");

        verify(postRepository, never()).save(any());
        verify(postReactionRepository, never()).delete(any());
    }

    @Test
    void getReactionsIncludesLikesSoTheFrontCanShowASingleList() {
        // A listagem no front é uma só: o tipo aparece no ícone, não em abas separadas.
        Pageable pageable = PageRequest.of(0, 10);
        when(postFinder.findVisibleActivePost(postId, userId)).thenReturn(post);
        when(postReactionRepository.findByPostId(postId, pageable))
                .thenReturn(new PageImpl<>(List.of(reaction(ReactionType.FIRE), reaction(ReactionType.LIKE)), pageable, 2));

        Page<PostReactionResponse> result = service.getReactions(userId, postId, pageable);

        assertThat(result.getContent()).extracting(PostReactionResponse::reactionType)
                .containsExactly(ReactionType.FIRE, ReactionType.LIKE);
        verify(userChecker).checkActive(user);
        verify(postReactionRepository, never()).findByPostIdAndReactionTypeNot(any(), any(), any());
    }

    @Test
    void getLikesMapsLikeReactionsWithProfiles() {
        Pageable pageable = PageRequest.of(0, 10);
        PostReaction reaction = reaction(ReactionType.LIKE);
        when(postFinder.findVisibleActivePost(postId, userId)).thenReturn(post);
        when(postReactionRepository.findByPostIdAndReactionType(postId, ReactionType.LIKE, pageable))
                .thenReturn(new PageImpl<>(List.of(reaction), pageable, 1));

        Page<PostReactionResponse> result = service.getLikes(userId, postId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).reactionType()).isEqualTo(ReactionType.LIKE);
        verify(userChecker).checkActive(user);
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
