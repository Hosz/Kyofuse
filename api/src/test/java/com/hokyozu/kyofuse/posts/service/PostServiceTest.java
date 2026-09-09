package com.hokyozu.kyofuse.posts.service;

import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.dto.response.PostResponse;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.entity.PostMap;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostType;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.posts.finder.PostFinder;
import com.hokyozu.kyofuse.posts.repository.PostMapRepository;
import com.hokyozu.kyofuse.posts.repository.PostMediaRepository;
import com.hokyozu.kyofuse.posts.repository.PostRepository;
import com.hokyozu.kyofuse.posts.validator.DeletePostValidator;
import com.hokyozu.kyofuse.posts.validator.PostMapsValidator;
import com.hokyozu.kyofuse.posts.validator.PostValidator;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
import com.hokyozu.kyofuse.relationships.permission.service.post.PostPermissionService;
import com.hokyozu.kyofuse.relationships.permission.service.profile.ProfilePermissionService;
import com.hokyozu.kyofuse.relationships.privacy.enums.ProfileVisibility;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

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

    @Mock
    private GamerProfileFinder gamerProfileFinder;

    @Mock
    private PostFinder postFinder;

    @Mock
    private UserFinder userFinder;

    @Mock
    private UserChecker userChecker;

    @Mock
    private PostPermissionService postPermissionService;

    @Mock
    private ProfilePermissionService profilePermissionService;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private CommunityMemberRepository communityMemberRepository;

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private PostMediaRepository postMediaRepository;

    @Mock
    private FeedRankingService feedRankingService;

    @Mock
    private MentionDetectionService mentionDetectionService;

    @Mock
    private com.hokyozu.kyofuse.reactions.repository.PostReactionRepository postReactionRepository;

    @InjectMocks
    private PostService postService;

    private static GamerProfile profileOf(User user) {
        return GamerProfile.builder().user(user).nickname("nickname").avatarUrl("avatar.png").build();
    }

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

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profile);
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

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profile);
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
        when(userFinder.findProfileByUserId(userId))
                .thenThrow(new RuntimeException("Gamer profile not found for user ID: " + userId));

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
        when(userFinder.findProfileByUserId(requesterId)).thenReturn(post.getAuthor());
        when(postFinder.findVisiblePostForUser(postId, requesterId, PostStatus.ACTIVE, PostVisibility.PUBLIC))
                .thenReturn(post);
        when(postMapRepository.findByPostId(postId)).thenReturn(postMaps);
        when(gamerProfileFinder.findProfileByUserId(requesterId)).thenReturn(profileOf(post.getAuthor()));

        PostResponse response = postService.getPost(requesterId, postId);

        verify(postFinder).findVisiblePostForUser(postId, requesterId, PostStatus.ACTIVE, PostVisibility.PUBLIC);
        verify(postMapRepository).findByPostId(postId);
        assertThat(response.id()).isEqualTo(postId);
        assertThat(response.authorId()).isEqualTo(requesterId);
        assertThat(response.maps()).containsExactly("MIRAGE");
    }

    @Test
    void getPostThrowsBadRequestWhenPostIsNotVisibleForRequester() {
        UUID requesterId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        when(userFinder.findProfileByUserId(requesterId)).thenReturn(User.builder().id(requesterId).build());
        when(postFinder.findVisiblePostForUser(postId, requesterId, PostStatus.ACTIVE, PostVisibility.PUBLIC))
                .thenThrow(new BadRequestException("Post not found for ID: " + postId));

        assertThatThrownBy(() -> postService.getPost(requesterId, postId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Post not found for ID: " + postId);
        verify(postMapRepository, never()).findByPostId(any());
    }

    @Test
    void getFeedReturnsOnlyPublicActivePostsAndMapsResponses() {
        Pageable pageable = PageRequest.of(0, 20);
        UUID userId = UUID.randomUUID();
        Post firstPost = post(UUID.randomUUID(), UUID.randomUUID(), PostVisibility.PUBLIC, PostStatus.ACTIVE);
        Post secondPost = post(UUID.randomUUID(), UUID.randomUUID(), PostVisibility.PUBLIC, PostStatus.ACTIVE);
        when(userFinder.findProfileByUserId(userId)).thenReturn(User.builder().id(userId).build());
        when(postRepository.findGlobalFeed(eq(PostVisibility.PUBLIC), eq(PostStatus.ACTIVE), eq(ProfileVisibility.PUBLIC), eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(firstPost, secondPost), pageable, 2));
        when(feedRankingService.rankPosts(anyList(), any())).thenAnswer(inv -> inv.getArgument(0));
        when(postMapRepository.findByPostIdIn(List.of(firstPost.getId(), secondPost.getId())))
                .thenReturn(List.of(postMap(firstPost, "MIRAGE"), postMap(secondPost, "INFERNO")));
        when(gamerProfileFinder.findAllByUserIds(List.of(firstPost.getAuthor().getId(), secondPost.getAuthor().getId())))
                .thenReturn(List.of(profileOf(firstPost.getAuthor()), profileOf(secondPost.getAuthor())));

        Page<PostResponse> response = postService.getFeed(pageable, userId);

        verify(postRepository).findGlobalFeed(eq(PostVisibility.PUBLIC), eq(PostStatus.ACTIVE), eq(ProfileVisibility.PUBLIC), eq(userId), any(Pageable.class));
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent()).extracting(PostResponse::id)
                .containsExactly(firstPost.getId(), secondPost.getId());
        assertThat(response.getContent()).extracting(PostResponse::maps)
                .containsExactly(List.of("MIRAGE"), List.of("INFERNO"));
    }

    @Test
    void getProfilePostsReturnsOnlyPublicActivePostsForAuthor() {
        UUID authorId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(1, 10);
        Post post = post(UUID.randomUUID(), authorId, PostVisibility.PUBLIC, PostStatus.ACTIVE);
        User viewer = User.builder().id(userId).build();
        when(userFinder.findProfileByUserId(userId)).thenReturn(viewer);
        when(userFinder.findProfileByUserId(authorId)).thenReturn(post.getAuthor());
        when(postPermissionService.canViewAuthorPosts(viewer, post.getAuthor())).thenReturn(true);
        when(postRepository.findByAuthorIdAndVisibilityInAndStatusAndCommunityIsNull(
                authorId,
                List.of(PostVisibility.PUBLIC),
                PostStatus.ACTIVE,
                pageable
        )).thenReturn(new PageImpl<>(List.of(post), pageable, 1));
        when(postMapRepository.findByPostIdIn(List.of(post.getId()))).thenReturn(List.of(postMap(post, "NUKE")));
        when(gamerProfileFinder.findAllByUserIds(List.of(authorId))).thenReturn(List.of(profileOf(post.getAuthor())));

        Page<PostResponse> response = postService.getProfilePosts(authorId, pageable, userId);

        verify(postRepository).findByAuthorIdAndVisibilityInAndStatusAndCommunityIsNull(
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
    void getProfileMediaPostsReturnsPublicActivePostsWithMedia() {
        UUID authorId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(1, 10);
        Post post = post(UUID.randomUUID(), authorId, PostVisibility.PUBLIC, PostStatus.ACTIVE);
        User viewer = User.builder().id(userId).build();
        when(userFinder.findProfileByUserId(userId)).thenReturn(viewer);
        when(userFinder.findProfileByUserId(authorId)).thenReturn(post.getAuthor());
        when(postPermissionService.canViewAuthorPosts(viewer, post.getAuthor())).thenReturn(true);
        when(postRepository.findMediaPostsByAuthorIdAndVisibilityInAndStatus(
                authorId,
                List.of(PostVisibility.PUBLIC),
                PostStatus.ACTIVE,
                pageable
        )).thenReturn(new PageImpl<>(List.of(post), pageable, 1));
        when(postMapRepository.findByPostIdIn(List.of(post.getId()))).thenReturn(List.of());
        when(gamerProfileFinder.findAllByUserIds(List.of(authorId))).thenReturn(List.of(profileOf(post.getAuthor())));

        Page<PostResponse> response = postService.getProfileMediaPosts(authorId, pageable, userId);

        verify(postRepository).findMediaPostsByAuthorIdAndVisibilityInAndStatus(
                authorId,
                List.of(PostVisibility.PUBLIC),
                PostStatus.ACTIVE,
                pageable
        );
        assertThat(response.getContent()).singleElement()
                .satisfies(postResponse -> {
                    assertThat(postResponse.id()).isEqualTo(post.getId());
                });
    }

    @Test
    void getMyPostsIncludesOwnPublicPrivateActiveAndHiddenPosts() {
        UUID authorId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 5);
        Post hiddenPost = post(UUID.randomUUID(), authorId, PostVisibility.PRIVATE, PostStatus.HIDDEN);
        when(userFinder.findProfileByUserId(authorId)).thenReturn(hiddenPost.getAuthor());
        when(postRepository.findByAuthorIdAndVisibilityInAndStatusInAndCommunityIsNull(
                eq(authorId),
                anyList(),
                anyList(),
                eq(pageable)
        )).thenReturn(new PageImpl<>(List.of(hiddenPost), pageable, 1));
        when(postMapRepository.findByPostIdIn(List.of(hiddenPost.getId()))).thenReturn(List.of());
        when(gamerProfileFinder.findAllByUserIds(List.of(authorId))).thenReturn(List.of(profileOf(hiddenPost.getAuthor())));

        Page<PostResponse> response = postService.getMyPosts(authorId, pageable);

        ArgumentCaptor<List<PostVisibility>> visibilityCaptor = visibilityListCaptor();
        ArgumentCaptor<List<PostStatus>> statusCaptor = statusListCaptor();
        verify(postRepository).findByAuthorIdAndVisibilityInAndStatusInAndCommunityIsNull(
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
    void getMyMediaPostsIncludesOwnPublicPrivateActiveAndHiddenMediaPosts() {
        UUID authorId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 5);
        Post mediaPost = post(UUID.randomUUID(), authorId, PostVisibility.PRIVATE, PostStatus.ACTIVE);
        when(userFinder.findProfileByUserId(authorId)).thenReturn(mediaPost.getAuthor());
        when(postRepository.findMyMediaPosts(
                eq(authorId),
                anyList(),
                anyList(),
                eq(pageable)
        )).thenReturn(new PageImpl<>(List.of(mediaPost), pageable, 1));
        when(postMapRepository.findByPostIdIn(List.of(mediaPost.getId()))).thenReturn(List.of());
        when(gamerProfileFinder.findAllByUserIds(List.of(authorId))).thenReturn(List.of(profileOf(mediaPost.getAuthor())));

        Page<PostResponse> response = postService.getMyMediaPosts(authorId, pageable);

        ArgumentCaptor<List<PostVisibility>> visibilityCaptor = visibilityListCaptor();
        ArgumentCaptor<List<PostStatus>> statusCaptor = statusListCaptor();
        verify(postRepository).findMyMediaPosts(
                eq(authorId),
                visibilityCaptor.capture(),
                statusCaptor.capture(),
                eq(pageable)
        );
        assertThat(visibilityCaptor.getValue()).containsExactly(PostVisibility.PUBLIC, PostVisibility.PRIVATE);
        assertThat(statusCaptor.getValue()).containsExactly(PostStatus.ACTIVE, PostStatus.HIDDEN);
        assertThat(response.getContent()).singleElement()
                .satisfies(postResponse -> {
                    assertThat(postResponse.id()).isEqualTo(mediaPost.getId());
                });
    }

    @Test
    void deletePostMarksPostAsDeletedAfterValidation() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Post post = post(postId, userId, PostVisibility.PUBLIC, PostStatus.ACTIVE);
        when(userFinder.findProfileByUserId(userId)).thenReturn(post.getAuthor());
        when(postFinder.findById(postId)).thenReturn(post);

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
        when(userFinder.findProfileByUserId(userId)).thenReturn(User.builder().id(userId).build());
        when(postFinder.findById(postId))
                .thenThrow(new BadRequestException("Post not found for ID: " + postId));

        assertThatThrownBy(() -> postService.deletePost(userId, postId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Post not found for ID: " + postId);

        verify(deletePostValidator, never()).validate(any(), any());
        verify(postRepository, never()).save(any());
    }

    @Test
    void postInCommunitySavesPostLinkedToTheCommunityForActiveMember() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User user = User.builder().id(userId).username("player").build();
        Community community = community(communityId, CommunityStatus.ACTIVE);
        CreatePostRequest request = new CreatePostRequest("hi community", PostType.TEXT, PostVisibility.PRIVATE, List.of());

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profileOf(user));
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
        when(communityMemberRepository.findByUserIdAndCommunityId(userId, communityId))
                .thenReturn(Optional.of(membership(community, user, CommunityMemberStatus.ACTIVE)));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PostResponse response = postService.postInCommunity(userId, communityId, request);

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getCommunity()).isSameAs(community);
        assertThat(response.communityId()).isEqualTo(communityId);
    }

    @Test
    void postInCommunityRejectsNonMember() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User user = User.builder().id(userId).username("player").build();
        CreatePostRequest request = new CreatePostRequest("hi", PostType.TEXT, PostVisibility.PUBLIC, List.of());

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profileOf(user));
        when(communityRepository.findById(communityId))
                .thenReturn(Optional.of(community(communityId, CommunityStatus.ACTIVE)));
        when(communityMemberRepository.findByUserIdAndCommunityId(userId, communityId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.postInCommunity(userId, communityId, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only active members can post in this community");

        verify(postRepository, never()).save(any());
    }

    @Test
    void postInCommunityRejectsArchivedCommunity() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User user = User.builder().id(userId).username("player").build();
        CreatePostRequest request = new CreatePostRequest("hi", PostType.TEXT, PostVisibility.PUBLIC, List.of());

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profileOf(user));
        when(communityRepository.findById(communityId))
                .thenReturn(Optional.of(community(communityId, CommunityStatus.ARCHIVED)));

        assertThatThrownBy(() -> postService.postInCommunity(userId, communityId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Community not found");
    }

    @Test
    void getCommunityPostsShowsPublicAndPrivateToActiveMember() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User user = User.builder().id(userId).username("player").build();
        Community community = community(communityId, CommunityStatus.ACTIVE);
        Pageable pageable = PageRequest.of(0, 20);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
        when(communityMemberRepository.findByUserIdAndCommunityId(userId, communityId))
                .thenReturn(Optional.of(membership(community, user, CommunityMemberStatus.ACTIVE)));
        when(postRepository.findByCommunityIdAndVisibilityInAndStatus(
                communityId, List.of(PostVisibility.PUBLIC, PostVisibility.PRIVATE), PostStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        postService.getCommunityPosts(userId, communityId, pageable);

        verify(postRepository).findByCommunityIdAndVisibilityInAndStatus(
                communityId, List.of(PostVisibility.PUBLIC, PostVisibility.PRIVATE), PostStatus.ACTIVE, pageable);
    }

    @Test
    void getCommunityPostsHidesPrivateFromNonMember() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User user = User.builder().id(userId).username("outsider").build();
        Pageable pageable = PageRequest.of(0, 20);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.findById(communityId))
                .thenReturn(Optional.of(community(communityId, CommunityStatus.ACTIVE)));
        when(communityMemberRepository.findByUserIdAndCommunityId(userId, communityId)).thenReturn(Optional.empty());
        when(postRepository.findByCommunityIdAndVisibilityInAndStatus(
                communityId, List.of(PostVisibility.PUBLIC), PostStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        postService.getCommunityPosts(userId, communityId, pageable);

        verify(postRepository).findByCommunityIdAndVisibilityInAndStatus(
                communityId, List.of(PostVisibility.PUBLIC), PostStatus.ACTIVE, pageable);
    }

    private static Community community(UUID communityId, CommunityStatus status) {
        return Community.builder()
                .id(communityId)
                .name("Kyofuse CS2")
                .slug("kyofuse-cs2")
                .visibility(CommunityVisibility.PUBLIC)
                .status(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private static CommunityMember membership(Community community, User user, CommunityMemberStatus status) {
        return CommunityMember.builder()
                .id(UUID.randomUUID())
                .community(community)
                .user(user)
                .status(status)
                .joinedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void getFollowingPostsReturnsEmptyWhenUserFollowsNobody() {
        Pageable pageable = PageRequest.of(0, 20);
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userFollowRepository.findFollowedIdsByFollower(user)).thenReturn(List.of());

        Page<PostResponse> response = postService.getFollowingPosts(userId, pageable);

        assertThat(response.getContent()).isEmpty();
        verify(userChecker).checkActive(user);
        verify(postRepository, never()).findByAuthorIdInAndVisibilityAndStatusAndCommunityIsNull(
                anyList(), any(), any(), any());
    }

    @Test
    void getFollowingPostsReturnsEmptyWhenNoFollowedAuthorAllowsViewing() {
        // Seguir não basta: quem restringiu postsVisibility para FRIENDS ou PRIVATE sai
        // da consulta antes dela acontecer.
        Pageable pageable = PageRequest.of(0, 20);
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        User author = User.builder().id(authorId).build();
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userFollowRepository.findFollowedIdsByFollower(user)).thenReturn(List.of(authorId));
        when(userFinder.findAllByIds(List.of(authorId))).thenReturn(List.of(author));
        when(postPermissionService.filterViewableAuthors(user, List.of(author))).thenReturn(List.of());

        Page<PostResponse> response = postService.getFollowingPosts(userId, pageable);

        assertThat(response.getContent()).isEmpty();
        verify(postRepository, never()).findByAuthorIdInAndVisibilityAndStatusAndCommunityIsNull(
                anyList(), any(), any(), any());
    }

    @Test
    void getFollowingPostsQueriesOnlyAuthorsThatAllowViewing() {
        Pageable pageable = PageRequest.of(0, 20);
        UUID userId = UUID.randomUUID();
        UUID visibleAuthorId = UUID.randomUUID();
        UUID hiddenAuthorId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        User visibleAuthor = User.builder().id(visibleAuthorId).build();
        User hiddenAuthor = User.builder().id(hiddenAuthorId).build();
        List<UUID> followedIds = List.of(visibleAuthorId, hiddenAuthorId);
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userFollowRepository.findFollowedIdsByFollower(user)).thenReturn(followedIds);
        when(userFinder.findAllByIds(followedIds)).thenReturn(List.of(visibleAuthor, hiddenAuthor));
        when(postPermissionService.filterViewableAuthors(user, List.of(visibleAuthor, hiddenAuthor)))
                .thenReturn(List.of(visibleAuthor));
        when(postRepository.findByAuthorIdInAndVisibilityAndStatusAndCommunityIsNull(
                List.of(visibleAuthorId),
                PostVisibility.PUBLIC,
                PostStatus.ACTIVE,
                pageable
        )).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        postService.getFollowingPosts(userId, pageable);

        verify(postRepository).findByAuthorIdInAndVisibilityAndStatusAndCommunityIsNull(
                List.of(visibleAuthorId),
                PostVisibility.PUBLIC,
                PostStatus.ACTIVE,
                pageable
        );
    }

    @Test
    void getFollowingPostsLoadsFollowedAuthorsInASingleBatch() {
        // Guarda contra o N+1: um findProfileByUserId por autor faria o número de
        // consultas crescer junto com a quantidade de gente seguida.
        Pageable pageable = PageRequest.of(0, 20);
        UUID userId = UUID.randomUUID();
        UUID firstAuthorId = UUID.randomUUID();
        UUID secondAuthorId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        User firstAuthor = User.builder().id(firstAuthorId).build();
        User secondAuthor = User.builder().id(secondAuthorId).build();
        List<UUID> followedIds = List.of(firstAuthorId, secondAuthorId);
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userFollowRepository.findFollowedIdsByFollower(user)).thenReturn(followedIds);
        when(userFinder.findAllByIds(followedIds)).thenReturn(List.of(firstAuthor, secondAuthor));
        when(postPermissionService.filterViewableAuthors(user, List.of(firstAuthor, secondAuthor)))
                .thenReturn(List.of(firstAuthor, secondAuthor));
        when(postRepository.findByAuthorIdInAndVisibilityAndStatusAndCommunityIsNull(
                anyList(), any(), any(), any()
        )).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        postService.getFollowingPosts(userId, pageable);

        verify(userFinder).findAllByIds(followedIds);
        verify(postPermissionService).filterViewableAuthors(user, List.of(firstAuthor, secondAuthor));
        verify(userFinder, never()).findProfileByUserId(firstAuthorId);
        verify(userFinder, never()).findProfileByUserId(secondAuthorId);
        verify(postPermissionService, never()).canViewAuthorPosts(any(), any());
    }

    @Test
    void getFollowingPostsMapsPostsWithMapsAndAuthorProfile() {
        Pageable pageable = PageRequest.of(0, 20);
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        User author = User.builder().id(authorId).username("player").build();
        Post post = post(UUID.randomUUID(), authorId, PostVisibility.PUBLIC, PostStatus.ACTIVE);
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userFollowRepository.findFollowedIdsByFollower(user)).thenReturn(List.of(authorId));
        when(userFinder.findAllByIds(List.of(authorId))).thenReturn(List.of(author));
        when(postPermissionService.filterViewableAuthors(user, List.of(author))).thenReturn(List.of(author));
        when(postRepository.findByAuthorIdInAndVisibilityAndStatusAndCommunityIsNull(
                List.of(authorId),
                PostVisibility.PUBLIC,
                PostStatus.ACTIVE,
                pageable
        )).thenReturn(new PageImpl<>(List.of(post), pageable, 1));
        when(postMapRepository.findByPostIdIn(List.of(post.getId()))).thenReturn(List.of(postMap(post, "ANCIENT")));
        when(gamerProfileFinder.findAllByUserIds(List.of(authorId))).thenReturn(List.of(profileOf(post.getAuthor())));

        Page<PostResponse> response = postService.getFollowingPosts(userId, pageable);

        assertThat(response.getContent()).singleElement()
                .satisfies(postResponse -> {
                    assertThat(postResponse.id()).isEqualTo(post.getId());
                    assertThat(postResponse.authorId()).isEqualTo(authorId);
                    assertThat(postResponse.authorNickname()).isEqualTo("nickname");
                    assertThat(postResponse.maps()).containsExactly("ANCIENT");
                });
    }

    @Test
    void getFollowingPostsRejectsInactiveUser() {
        Pageable pageable = PageRequest.of(0, 20);
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        doThrow(new ForbiddenException("User is not active.")).when(userChecker).checkActive(user);

        assertThatThrownBy(() -> postService.getFollowingPosts(userId, pageable))
                .isInstanceOf(ForbiddenException.class);

        verify(userFollowRepository, never()).findFollowedIdsByFollower(any());
    }

    @Test
    void getFeedAsksForTheViewerIdSoOwnPostsAreNeverFilteredOut() {
        Pageable pageable = PageRequest.of(0, 20);
        UUID userId = UUID.randomUUID();
        when(userFinder.findProfileByUserId(userId)).thenReturn(User.builder().id(userId).build());
        when(postRepository.findGlobalFeed(eq(PostVisibility.PUBLIC), eq(PostStatus.ACTIVE), eq(ProfileVisibility.PUBLIC), eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        when(feedRankingService.rankPosts(anyList(), any())).thenAnswer(inv -> inv.getArgument(0));

        postService.getFeed(pageable, userId);

        verify(postRepository).findGlobalFeed(
                eq(PostVisibility.PUBLIC), eq(PostStatus.ACTIVE), eq(ProfileVisibility.PUBLIC), eq(userId), any(Pageable.class));
    }

    @Test
    void feedPageLoadsMapsAndProfilesInASingleBatchEach() {
        Pageable pageable = PageRequest.of(0, 20);
        UUID userId = UUID.randomUUID();
        Post firstPost = post(UUID.randomUUID(), UUID.randomUUID(), PostVisibility.PUBLIC, PostStatus.ACTIVE);
        Post secondPost = post(UUID.randomUUID(), UUID.randomUUID(), PostVisibility.PUBLIC, PostStatus.ACTIVE);
        Post thirdPost = post(UUID.randomUUID(), UUID.randomUUID(), PostVisibility.PUBLIC, PostStatus.ACTIVE);
        List<Post> posts = List.of(firstPost, secondPost, thirdPost);
        when(userFinder.findProfileByUserId(userId)).thenReturn(User.builder().id(userId).build());
        when(postRepository.findGlobalFeed(eq(PostVisibility.PUBLIC), eq(PostStatus.ACTIVE), eq(ProfileVisibility.PUBLIC), eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(posts, pageable, posts.size()));
        when(feedRankingService.rankPosts(anyList(), any())).thenAnswer(inv -> inv.getArgument(0));
        when(postMapRepository.findByPostIdIn(anyList())).thenReturn(List.of());
        when(gamerProfileFinder.findAllByUserIds(anyList()))
                .thenReturn(posts.stream().map(post -> profileOf(post.getAuthor())).toList());

        postService.getFeed(pageable, userId);

        verify(postMapRepository).findByPostIdIn(
                List.of(firstPost.getId(), secondPost.getId(), thirdPost.getId()));
        verify(gamerProfileFinder).findAllByUserIds(List.of(
                firstPost.getAuthor().getId(), secondPost.getAuthor().getId(), thirdPost.getAuthor().getId()));
        verify(postMapRepository, never()).findByPostId(any());
        verify(gamerProfileFinder, times(1)).findProfileByUserId(userId);
        verify(gamerProfileFinder, never()).findProfileByUserId(firstPost.getAuthor().getId());
        verify(gamerProfileFinder, never()).findProfileByUserId(secondPost.getAuthor().getId());
        verify(gamerProfileFinder, never()).findProfileByUserId(thirdPost.getAuthor().getId());
    }

    @Test
    void feedPageAsksForEachAuthorProfileOnlyOnceWhenTheSameAuthorRepeats() {
        Pageable pageable = PageRequest.of(0, 20);
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Post firstPost = post(UUID.randomUUID(), authorId, PostVisibility.PUBLIC, PostStatus.ACTIVE);
        Post secondPost = post(UUID.randomUUID(), authorId, PostVisibility.PUBLIC, PostStatus.ACTIVE);
        when(userFinder.findProfileByUserId(userId)).thenReturn(User.builder().id(userId).build());
        when(postRepository.findGlobalFeed(eq(PostVisibility.PUBLIC), eq(PostStatus.ACTIVE), eq(ProfileVisibility.PUBLIC), eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(firstPost, secondPost), pageable, 2));
        when(feedRankingService.rankPosts(anyList(), any())).thenAnswer(inv -> inv.getArgument(0));
        when(postMapRepository.findByPostIdIn(anyList())).thenReturn(List.of());
        when(gamerProfileFinder.findAllByUserIds(List.of(authorId)))
                .thenReturn(List.of(profileOf(firstPost.getAuthor())));

        Page<PostResponse> response = postService.getFeed(pageable, userId);

        verify(gamerProfileFinder).findAllByUserIds(List.of(authorId));
        assertThat(response.getContent()).extracting(PostResponse::authorNickname)
                .containsExactly("nickname", "nickname");
    }

    @Test
    void feedPageGroupsMapsByTheirOwnPost() {
        Pageable pageable = PageRequest.of(0, 20);
        UUID userId = UUID.randomUUID();
        Post firstPost = post(UUID.randomUUID(), UUID.randomUUID(), PostVisibility.PUBLIC, PostStatus.ACTIVE);
        Post secondPost = post(UUID.randomUUID(), UUID.randomUUID(), PostVisibility.PUBLIC, PostStatus.ACTIVE);
        when(userFinder.findProfileByUserId(userId)).thenReturn(User.builder().id(userId).build());
        when(postRepository.findGlobalFeed(eq(PostVisibility.PUBLIC), eq(PostStatus.ACTIVE), eq(ProfileVisibility.PUBLIC), eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(firstPost, secondPost), pageable, 2));
        when(feedRankingService.rankPosts(anyList(), any())).thenAnswer(inv -> inv.getArgument(0));
        when(postMapRepository.findByPostIdIn(anyList())).thenReturn(List.of(
                postMap(secondPost, "INFERNO"),
                postMap(firstPost, "MIRAGE"),
                postMap(secondPost, "NUKE")
        ));
        when(gamerProfileFinder.findAllByUserIds(anyList()))
                .thenReturn(List.of(profileOf(firstPost.getAuthor()), profileOf(secondPost.getAuthor())));

        Page<PostResponse> response = postService.getFeed(pageable, userId);

        assertThat(response.getContent()).extracting(PostResponse::maps)
                .containsExactly(List.of("MIRAGE"), List.of("INFERNO", "NUKE"));
    }

    @Test
    void feedPageFallsBackToTheSingleFinderForAnAuthorMissingFromTheBatch() {
        Pageable pageable = PageRequest.of(0, 20);
        UUID userId = UUID.randomUUID();
        Post post = post(UUID.randomUUID(), UUID.randomUUID(), PostVisibility.PUBLIC, PostStatus.ACTIVE);
        UUID authorId = post.getAuthor().getId();
        when(userFinder.findProfileByUserId(userId)).thenReturn(User.builder().id(userId).build());
        when(gamerProfileFinder.findProfileByUserId(userId)).thenReturn(profileOf(User.builder().id(userId).build()));
        when(postRepository.findGlobalFeed(eq(PostVisibility.PUBLIC), eq(PostStatus.ACTIVE), eq(ProfileVisibility.PUBLIC), eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(post), pageable, 1));
        when(feedRankingService.rankPosts(anyList(), any())).thenAnswer(inv -> inv.getArgument(0));
        when(postMapRepository.findByPostIdIn(anyList())).thenReturn(List.of());
        when(gamerProfileFinder.findAllByUserIds(List.of(authorId))).thenReturn(List.of());
        when(gamerProfileFinder.findProfileByUserId(authorId))
                .thenThrow(new RuntimeException("Gamer profile not found for user ID: " + authorId));

        assertThatThrownBy(() -> postService.getFeed(pageable, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Gamer profile not found for user ID: " + authorId);
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
