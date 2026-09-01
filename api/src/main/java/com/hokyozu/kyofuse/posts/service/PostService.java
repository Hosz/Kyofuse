package com.hokyozu.kyofuse.posts.service;

import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.dto.response.PostResponse;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.entity.PostMap;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.posts.finder.PostFinder;
import com.hokyozu.kyofuse.posts.mapper.PostMapper;
import com.hokyozu.kyofuse.posts.repository.PostMapRepository;
import com.hokyozu.kyofuse.posts.repository.PostRepository;
import com.hokyozu.kyofuse.posts.validator.DeletePostValidator;
import com.hokyozu.kyofuse.posts.validator.PostMapsValidator;
import com.hokyozu.kyofuse.posts.validator.PostValidator;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
import com.hokyozu.kyofuse.relationships.permission.service.post.PostPermissionService;
import com.hokyozu.kyofuse.relationships.permission.service.profile.ProfilePermissionService;
import com.hokyozu.kyofuse.relationships.privacy.enums.ProfileVisibility;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import com.hokyozu.kyofuse.posts.entity.PostMedia;
import com.hokyozu.kyofuse.posts.repository.PostMediaRepository;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostMapRepository postMapRepository;
    private final PostMediaRepository postMediaRepository;

    private final PostValidator postValidator;
    private final PostMapsValidator postMapsValidator;
    private final DeletePostValidator deletePostValidator;

    private final ProfilePermissionService profilePermissionService;

    private final PostFinder postFinder;
    private final UserFinder userFinder;
    private final UserChecker userChecker;
    private final PostPermissionService postPermissionService;
    private final GamerProfileFinder gamerProfileFinder;
    private final FeedRankingService feedRankingService;
    private final MentionDetectionService mentionDetectionService;

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final UserFollowRepository userFollowRepository;

    @Transactional
    public PostResponse post(UUID userId, CreatePostRequest request) {
        User user = userFinder.findProfileByUserId(userId);
        GamerProfile gamerProfile = gamerProfileFinder.findProfileByUserId(user.getId());
        userChecker.checkActive(user);

        postValidator.validate(request);
        postMapsValidator.validate(request);

        Post post = PostMapper.toEntity(user, request);
        Post savedPost = postRepository.save(post);

        List<PostMap> postMaps = PostMapper.toPostMap(post, request.maps());
        if (!postMaps.isEmpty()) {
            postMapRepository.saveAll(postMaps);
        }

        List<PostMedia> postMedias = savePostMedia(savedPost, request);

        mentionDetectionService.processMentions(savedPost);
        return PostMapper.toResponse(savedPost, postMaps, postMedias, gamerProfile);
    }

    /**
     * Publica um post exclusivo de uma comunidade. Só membro ACTIVE pode publicar,
     * independentemente da visibilidade escolhida — ver doc.md 10.5.
     */
    @Transactional
    public PostResponse postInCommunity(UUID userId, UUID communityId, CreatePostRequest request) {
        User user = userFinder.findProfileByUserId(userId);
        GamerProfile gamerProfile = gamerProfileFinder.findProfileByUserId(user.getId());
        userChecker.checkActive(user);

        Community community = activeCommunity(communityId);

        if (!isActiveMember(userId, communityId)) {
            throw new ForbiddenException("Only active members can post in this community");
        }

        postValidator.validate(request);
        postMapsValidator.validate(request);

        Post savedPost = postRepository.save(PostMapper.toEntity(user, request, community));

        List<PostMap> postMaps = PostMapper.toPostMap(savedPost, request.maps());
        if (!postMaps.isEmpty()) {
            postMapRepository.saveAll(postMaps);
        }

        List<PostMedia> postMedias = savePostMedia(savedPost, request);

        mentionDetectionService.processMentions(savedPost);
        return PostMapper.toResponse(savedPost, postMaps, postMedias, gamerProfile);
    }

    private List<PostMedia> savePostMedia(Post post, CreatePostRequest request) {
        if (request.media() == null || request.media().isEmpty()) {
            return List.of();
        }

        List<PostMedia> mediaEntities = request.media().stream()
                .map(item -> PostMedia.builder()
                        .post(post)
                        .fileKey(item.fileKey())
                        .url(item.url())
                        .thumbnailUrl(item.thumbnailUrl())
                        .contentType(item.contentType())
                        .fileSizeBytes(item.fileSizeBytes())
                        .width(item.width())
                        .height(item.height())
                        .displayOrder(item.displayOrder() != null ? item.displayOrder() : 0)
                        .createdAt(Instant.now())
                        .build())
                .toList();

        return postMediaRepository.saveAll(mediaEntities);
    }

    /**
     * Posts de uma comunidade. Membro ACTIVE enxerga PUBLIC e PRIVATE; quem não é membro
     * enxerga apenas os PUBLIC.
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getCommunityPosts(UUID userId, UUID communityId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        activeCommunity(communityId);

        List<PostVisibility> visibilities = isActiveMember(userId, communityId)
                ? List.of(PostVisibility.PUBLIC, PostVisibility.PRIVATE)
                : List.of(PostVisibility.PUBLIC);

        return toResponsePage(postRepository
                .findByCommunityIdAndVisibilityInAndStatus(communityId, visibilities, PostStatus.ACTIVE, pageable));
    }

    /**
     * Monta as respostas de uma página inteira com consultas em lote — os mapas, as mídias e os
     * perfis de todos os autores — em vez de N consultas por post.
     */
    private Page<PostResponse> toResponsePage(Page<Post> postsPage) {
        List<Post> posts = postsPage.getContent();

        List<UUID> postIds = posts.stream().map(Post::getId).toList();
        List<UUID> authorIds = posts.stream().map(post -> post.getAuthor().getId()).distinct().toList();

        Map<UUID, List<PostMap>> mapsByPost = postIds.isEmpty()
                ? Map.of()
                : postMapRepository.findByPostIdIn(postIds).stream()
                        .collect(Collectors.groupingBy(postMap -> postMap.getPost().getId()));

        Map<UUID, List<PostMedia>> mediaByPost = postIds.isEmpty()
                ? Map.of()
                : postMediaRepository.findByPostIdInOrderByDisplayOrderAsc(postIds).stream()
                        .collect(Collectors.groupingBy(postMedia -> postMedia.getPost().getId()));

        Map<UUID, GamerProfile> profilesByAuthor = gamerProfileFinder.findAllByUserIds(authorIds).stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), profile -> profile));

        return postsPage.map(post -> PostMapper.toResponse(
                post,
                mapsByPost.getOrDefault(post.getId(), List.of()),
                mediaByPost.getOrDefault(post.getId(), List.of()),
                profilesByAuthor.computeIfAbsent(
                        post.getAuthor().getId(),
                        gamerProfileFinder::findProfileByUserId
                )
        ));
    }

    private Community activeCommunity(UUID communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new NotFoundException("Community not found"));

        if (community.getStatus() == CommunityStatus.ARCHIVED) {
            throw new NotFoundException("Community not found");
        }

        return community;
    }

    private boolean isActiveMember(UUID userId, UUID communityId) {
        return communityMemberRepository.findByUserIdAndCommunityId(userId, communityId)
                .filter(member -> member.getStatus() == CommunityMemberStatus.ACTIVE)
                .isPresent();
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(UUID userId, UUID postId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Post post = postFinder.findVisiblePostForUser(postId, userId, PostStatus.ACTIVE, PostVisibility.PUBLIC);
        postPermissionService.validateViewPost(user, post);

        List<PostMap> postMaps = postMapRepository.findByPostId(postId);
        List<PostMedia> postMedias = postMediaRepository.findByPostIdOrderByDisplayOrderAsc(postId);

        GamerProfile gamerProfile = gamerProfileFinder.findProfileByUserId(post.getAuthor().getId());
        return PostMapper.toResponse(post, postMaps, postMedias, gamerProfile);
    }

    /**
     * Feed geral/anônimo. Só entra quem deixou postsVisibility PUBLIC — ver
     * PostRepository.findGlobalFeed.
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getFeed(Pageable pageable, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);
        GamerProfile viewerProfile = gamerProfileFinder.findProfileByUserId(userId);

        // Limit candidates pool to the recent 200 posts to avoid full table scanning for scores
        Pageable candidatePageable = PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Post> candidatesPage = postRepository.findGlobalFeed(
                PostVisibility.PUBLIC,
                PostStatus.ACTIVE,
                ProfileVisibility.PUBLIC,
                user.getId(),
                candidatePageable
        );

        List<Post> rankedPosts = feedRankingService.rankPosts(candidatesPage.getContent(), viewerProfile);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), rankedPosts.size());
        
        List<Post> pagedPosts = new java.util.ArrayList<>();
        if (start < rankedPosts.size()) {
            pagedPosts = rankedPosts.subList(start, end);
        }

        Page<Post> rankedPage = new PageImpl<>(pagedPosts, pageable, rankedPosts.size());

        return toResponsePage(rankedPage);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getProfilePosts(UUID profileId, Pageable pageable, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        User profileOwner = userFinder.findProfileByUserId(profileId);

        if (userId.equals(profileId)) {
            return getMyPosts(profileId, pageable);
        }

        profilePermissionService.validateViewPosts(user, profileOwner);

        if (!postPermissionService.canViewAuthorPosts(user, profileOwner)) {
            throw new ForbiddenException("User does not have permission to view this user's posts.");
        }

        Page<Post> postsPage = postRepository.findByAuthorIdAndVisibilityInAndStatusAndCommunityIsNull(
                profileOwner.getId(),
                List.of(PostVisibility.PUBLIC),
                PostStatus.ACTIVE,
                pageable
        );

        return toResponsePage(postsPage);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getProfileMediaPosts(UUID profileId, Pageable pageable, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        User profileOwner = userFinder.findProfileByUserId(profileId);

        if (userId.equals(profileId)) {
            return getMyMediaPosts(profileId, pageable);
        }

        profilePermissionService.validateViewPosts(user, profileOwner);

        if (!postPermissionService.canViewAuthorPosts(user, profileOwner)) {
            throw new ForbiddenException("User does not have permission to view this user's posts.");
        }

        Page<Post> postsPage = postRepository.findMediaPostsByAuthorIdAndVisibilityInAndStatus(
                profileOwner.getId(),
                List.of(PostVisibility.PUBLIC),
                PostStatus.ACTIVE,
                pageable
        );

        return toResponsePage(postsPage);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getMyPosts(UUID authorId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(authorId);
        userChecker.checkActive(user);

        Page<Post> postsPage = postRepository.findByAuthorIdAndVisibilityInAndStatusInAndCommunityIsNull(
                authorId,
                List.of(PostVisibility.PUBLIC, PostVisibility.PRIVATE),
                List.of(PostStatus.ACTIVE, PostStatus.HIDDEN),
                pageable
        );

        return toResponsePage(postsPage);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getMyMediaPosts(UUID authorId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(authorId);
        userChecker.checkActive(user);

        Page<Post> postsPage = postRepository.findMyMediaPosts(
                authorId,
                List.of(PostVisibility.PUBLIC, PostVisibility.PRIVATE),
                List.of(PostStatus.ACTIVE, PostStatus.HIDDEN),
                pageable
        );

        return toResponsePage(postsPage);
    }

    @Transactional
    public void deletePost(UUID userId, UUID postId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Post post = postFinder.findById(postId);

        deletePostValidator.validate(post, userId);

        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
    }

    /**
     * Posts de quem o usuário segue. Só PUBLIC entra aqui — PRIVATE é visível apenas
     * para o próprio autor (mesma regra de getProfilePosts/getFeed), seguir alguém não
     * dá acesso aos posts privados dela. Seguir também não basta se o autor restringiu
     * postsVisibility para FRIENDS ou PRIVATE — só amizade mútua (ou ninguém) libera.
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getFollowingPosts(UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        List<UUID> followedIds = userFollowRepository.findFollowedIdsByFollower(user);
        if (followedIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // Busca e checagem em lote: um autor por consulta faria o número de queries
        // crescer junto com a quantidade de gente que o usuário segue.
        List<User> followedUsers = userFinder.findAllByIds(followedIds);
        List<UUID> viewableAuthorIds = postPermissionService.filterViewableAuthors(user, followedUsers)
                .stream()
                .map(User::getId)
                .toList();

        if (viewableAuthorIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<Post> postsPage = postRepository.findByAuthorIdInAndVisibilityAndStatusAndCommunityIsNull(
                viewableAuthorIds,
                PostVisibility.PUBLIC,
                PostStatus.ACTIVE,
                pageable
        );

        return toResponsePage(postsPage);
    }
}
