package com.hokyozu.kyofuse.posts.service;

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
import com.hokyozu.kyofuse.relationships.permission.service.post.PostPermissionService;
import com.hokyozu.kyofuse.relationships.permission.service.profile.ProfilePermissionService;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostMapRepository postMapRepository;

    private final PostValidator postValidator;
    private final PostMapsValidator postMapsValidator;
    private final DeletePostValidator deletePostValidator;

    private final ProfilePermissionService profilePermissionService;

    private final PostFinder postFinder;
    private final UserFinder userFinder;
    private final UserChecker userChecker;
    private final PostPermissionService postPermissionService;
    private final GamerProfileFinder gamerProfileFinder;

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

        return PostMapper.toResponse(savedPost, postMaps, gamerProfile);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(UUID userId, UUID postId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Post post = postFinder.findVisiblePostForUser(postId, userId, PostStatus.ACTIVE, PostVisibility.PUBLIC);
        postPermissionService.validateViewPost(user, post);

        List<PostMap> postMaps = postMapRepository.findByPostId(postId);

        GamerProfile gamerProfile = gamerProfileFinder.findProfileByUserId(post.getAuthor().getId());
        return PostMapper.toResponse(post, postMaps, gamerProfile);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getFeed(Pageable pageable, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Page<Post> postsPage = postRepository.findByVisibilityAndStatus(
                PostVisibility.PUBLIC,
                PostStatus.ACTIVE,
                pageable
        );

        return postsPage.map(post -> {
            List<PostMap> postMaps = postMapRepository.findByPostId(post.getId());
            GamerProfile gamerProfile = gamerProfileFinder.findProfileByUserId(post.getAuthor().getId());
            return PostMapper.toResponse(post, postMaps, gamerProfile);
        });
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getProfilePosts(UUID profileId, Pageable pageable, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        User profileOwner = userFinder.findProfileByUserId(profileId);

        profilePermissionService.validateViewPosts(user, profileOwner);

        Page<Post> postsPage = postRepository.findByAuthorIdAndVisibilityInAndStatus(
                profileOwner.getId(),
                List.of(PostVisibility.PUBLIC),
                PostStatus.ACTIVE,
                pageable
        );

        return postsPage.map(post -> {
            List<PostMap> postMaps = postMapRepository.findByPostId(post.getId());
            GamerProfile gamerProfile = gamerProfileFinder.findProfileByUserId(post.getAuthor().getId());
            return PostMapper.toResponse(post, postMaps, gamerProfile);
        });
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getMyPosts(UUID authorId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(authorId);
        userChecker.checkActive(user);

        Page<Post> postsPage = postRepository.findByAuthorIdAndVisibilityInAndStatusIn(
                authorId,
                List.of(PostVisibility.PUBLIC, PostVisibility.PRIVATE),
                List.of(PostStatus.ACTIVE, PostStatus.HIDDEN),
                pageable
        );

        return postsPage.map(post -> {
            List<PostMap> postMaps = postMapRepository.findByPostId(post.getId());
            GamerProfile gamerProfile = gamerProfileFinder.findProfileByUserId(post.getAuthor().getId());
            return PostMapper.toResponse(post, postMaps, gamerProfile);
        });
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
}
