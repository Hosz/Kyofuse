package com.hokyozu.kyofuse.reactions.service;

import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.comments.finder.CommentFinder;
import com.hokyozu.kyofuse.comments.repository.CommentRepository;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.finder.PostFinder;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.reactions.dto.request.CommentReactionRequest;
import com.hokyozu.kyofuse.reactions.dto.response.CommentReactionResponse;
import com.hokyozu.kyofuse.reactions.entity.CommentReaction;
import com.hokyozu.kyofuse.reactions.enums.ReactionType;
import com.hokyozu.kyofuse.reactions.repository.CommentReactionRepository;
import com.hokyozu.kyofuse.relationships.permission.service.comment.CommentPermissionService;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentReactionServiceTest {

    @Mock private UserFinder userFinder;
    @Mock private PostFinder postFinder;
    @Mock private CommentFinder commentFinder;
    @Mock private CommentReactionRepository reactionRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private CommentPermissionService commentPermissionService;
    @Mock private GamerProfileFinder gamerProfileFinder;
    @Mock private UserChecker userChecker;
    @InjectMocks private CommentReactionService service;

    private UUID userId;
    private UUID postId;
    private UUID commentId;
    private User user;
    private Comment comment;
    private GamerProfile profile;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        user = User.builder().id(userId).username("player").build();
        comment = Comment.builder()
                .id(commentId)
                .post(Post.builder().id(postId).build())
                .likeCount(3)
                .reactionCount(4)
                .build();
        profile = GamerProfile.builder().nickname("PlayerNick").avatarUrl("avatar.png").build();
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        lenient().when(commentFinder.findById(commentId)).thenReturn(comment);
        lenient().when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profile);
    }

    @Test
    void createsLikeAndIncrementsLikeCount() {
        stubNewReaction();

        CommentReactionResponse response =
                service.upsertReaction(postId, commentId, request(ReactionType.LIKE), userId);

        assertThat(comment.getLikeCount()).isEqualTo(4);
        assertThat(comment.getReactionCount()).isEqualTo(4);
        assertThat(response).isEqualTo(
                new CommentReactionResponse(postId, commentId, "player", "PlayerNick", "avatar.png", ReactionType.LIKE));
        verify(commentRepository).save(comment);
    }

    @Test
    void createsNonLikeAndIncrementsReactionCount() {
        stubNewReaction();

        service.upsertReaction(postId, commentId, request(ReactionType.FIRE), userId);

        assertThat(comment.getLikeCount()).isEqualTo(3);
        assertThat(comment.getReactionCount()).isEqualTo(5);
    }

    @Test
    void changesNonLikeToLikeAndMovesCounters() {
        CommentReaction existing = reaction(ReactionType.FIRE);
        stubExisting(existing);

        service.upsertReaction(postId, commentId, request(ReactionType.LIKE), userId);

        assertThat(comment.getReactionCount()).isEqualTo(3);
        assertThat(comment.getLikeCount()).isEqualTo(4);
        assertThat(existing.getReactionType()).isEqualTo(ReactionType.LIKE);
        assertThat(existing.getUpdatedAt()).isAfter(Instant.EPOCH);
    }

    @Test
    void changesLikeToNonLikeAndMovesCounters() {
        CommentReaction existing = reaction(ReactionType.LIKE);
        stubExisting(existing);

        service.upsertReaction(postId, commentId, request(ReactionType.CLUTCH), userId);

        assertThat(comment.getLikeCount()).isEqualTo(2);
        assertThat(comment.getReactionCount()).isEqualTo(5);
        assertThat(existing.getReactionType()).isEqualTo(ReactionType.CLUTCH);
    }

    @Test
    void keepsCountersWhenReactionCategoryDoesNotChange() {
        CommentReaction existing = reaction(ReactionType.FIRE);
        stubExisting(existing);

        service.upsertReaction(postId, commentId, request(ReactionType.LOL), userId);

        assertThat(comment.getLikeCount()).isEqualTo(3);
        assertThat(comment.getReactionCount()).isEqualTo(4);
    }

    @Test
    void keepsCountersWhenLikeDoesNotChange() {
        CommentReaction existing = reaction(ReactionType.LIKE);
        stubExisting(existing);

        service.upsertReaction(postId, commentId, request(ReactionType.LIKE), userId);

        assertThat(comment.getLikeCount()).isEqualTo(3);
        assertThat(comment.getReactionCount()).isEqualTo(4);
    }

    @Test
    void removesLikeAndDecrementsLikeCount() {
        CommentReaction existing = reaction(ReactionType.LIKE);
        stubRemoval(existing);

        service.removeReaction(userId, postId, commentId);

        assertThat(comment.getLikeCount()).isEqualTo(2);
        assertThat(comment.getReactionCount()).isEqualTo(4);
        verify(commentRepository).save(comment);
        verify(reactionRepository).delete(existing);
    }

    @Test
    void removesNonLikeAndDecrementsReactionCount() {
        CommentReaction existing = reaction(ReactionType.FIRE);
        stubRemoval(existing);

        service.removeReaction(userId, postId, commentId);

        assertThat(comment.getLikeCount()).isEqualTo(3);
        assertThat(comment.getReactionCount()).isEqualTo(3);
        verify(reactionRepository).delete(existing);
    }

    @Test
    void rejectsRemovalWhenReactionDoesNotExist() {
        when(reactionRepository.findByComment_Post_IdAndComment_IdAndUserId(
                postId, commentId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeReaction(userId, postId, commentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Reação não encontrada.");

        verify(commentRepository, never()).save(any());
        verify(reactionRepository, never()).delete(any());
    }

    @Test
    void getLikesMapsLikeReactionsWithProfiles() {
        Pageable pageable = PageRequest.of(0, 10);
        CommentReaction existing = reaction(ReactionType.LIKE);
        when(reactionRepository.findByComment_Post_IdAndComment_IdAndReactionType(
                postId, commentId, ReactionType.LIKE, pageable))
                .thenReturn(new PageImpl<>(List.of(existing), pageable, 1));

        Page<CommentReactionResponse> result = service.getLikes(postId, commentId, userId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).reactionType()).isEqualTo(ReactionType.LIKE);
        verify(userChecker).checkActive(user);
    }

    @Test
    void getReactionsMapsNonLikeReactionsWithProfiles() {
        Pageable pageable = PageRequest.of(0, 10);
        CommentReaction existing = reaction(ReactionType.FIRE);
        when(reactionRepository.findByComment_Post_IdAndComment_IdAndReactionTypeNot(
                postId, commentId, ReactionType.LIKE, pageable))
                .thenReturn(new PageImpl<>(List.of(existing), pageable, 1));

        Page<CommentReactionResponse> result = service.getReactions(postId, commentId, userId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).reactionType()).isEqualTo(ReactionType.FIRE);
        verify(userChecker).checkActive(user);
    }

    private void stubNewReaction() {
        when(reactionRepository.findByComment_Post_IdAndComment_IdAndUserId(
                postId, commentId, userId)).thenReturn(Optional.empty());
        when(reactionRepository.save(any(CommentReaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void stubExisting(CommentReaction reaction) {
        when(reactionRepository.findByComment_Post_IdAndComment_IdAndUserId(
                postId, commentId, userId)).thenReturn(Optional.of(reaction));
        when(reactionRepository.save(reaction)).thenReturn(reaction);
    }

    private void stubRemoval(CommentReaction reaction) {
        when(reactionRepository.findByComment_Post_IdAndComment_IdAndUserId(
                postId, commentId, userId)).thenReturn(Optional.of(reaction));
    }

    private CommentReaction reaction(ReactionType type) {
        return CommentReaction.builder()
                .comment(comment)
                .user(user)
                .reactionType(type)
                .createdAt(Instant.EPOCH)
                .updatedAt(Instant.EPOCH)
                .build();
    }

    private CommentReactionRequest request(ReactionType type) {
        return new CommentReactionRequest(type);
    }
}
