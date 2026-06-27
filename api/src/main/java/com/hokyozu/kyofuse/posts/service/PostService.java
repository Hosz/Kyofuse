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

    private final GamerProfileFinder gamerProfileFinder;
    private final PostFinder postFinder;

    @Transactional
    public PostResponse post(UUID userId, CreatePostRequest request) {
        GamerProfile profile = gamerProfileFinder.findProfileByUserId(userId);

        postValidator.validate(request);
        postMapsValidator.validate(request);

        Post post = PostMapper.toEntity(profile, request);
        Post savedPost = postRepository.save(post);

        List<PostMap> postMaps = PostMapper.toPostMap(post, request.maps());

        if (!postMaps.isEmpty()) {
            postMapRepository.saveAll(postMaps);
        }

        return PostMapper.toResponse(savedPost, postMaps);
    }

    public PostResponse getPost(UUID userId, UUID postId) {
        Post post = postFinder.findVisiblePostForUser(postId, userId, PostStatus.ACTIVE, PostVisibility.PUBLIC);

        List<PostMap> postMaps = postMapRepository.findByPostId(postId);

        return PostMapper.toResponse(post, postMaps);
    }

    public Page<PostResponse> getFeed(Pageable pageable) {
        Page<Post> postsPage = postRepository.findByVisibilityAndStatus(
                PostVisibility.PUBLIC,
                PostStatus.ACTIVE,
                pageable
        );

        return postsPage.map(post -> {
            List<PostMap> postMaps = postMapRepository.findByPostId(post.getId());
            return PostMapper.toResponse(post, postMaps);
        });
    }

    public Page<PostResponse> getProfilePosts(UUID profileId, Pageable pageable) {
        Page<Post> postsPage = postRepository.findByAuthorIdAndVisibilityInAndStatus(
                profileId,
                List.of(PostVisibility.PUBLIC),
                PostStatus.ACTIVE,
                pageable
        );

        return postsPage.map(post -> {
            List<PostMap> postMaps = postMapRepository.findByPostId(post.getId());
            return PostMapper.toResponse(post, postMaps);
        });
    }

    public Page<PostResponse> getMyPosts(UUID authorId, Pageable pageable) {
        Page<Post> postsPage = postRepository.findByAuthorIdAndVisibilityInAndStatusIn(
                authorId,
                List.of(PostVisibility.PUBLIC, PostVisibility.PRIVATE),
                List.of(PostStatus.ACTIVE, PostStatus.HIDDEN),
                pageable
        );

        return postsPage.map(post -> {
            List<PostMap> postMaps = postMapRepository.findByPostId(post.getId());
            return PostMapper.toResponse(post, postMaps);
        });
    }

    @Transactional
    public void deletePost(UUID userId, UUID postId) {
        Post post = postFinder.findById(postId);

        deletePostValidator.validate(post, userId);

        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
    }
}
