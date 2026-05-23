package com.hokyozu.kyofuse.posts.service;

import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.dto.response.PostResponse;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.entity.PostMap;
import com.hokyozu.kyofuse.posts.mapper.PostMapper;
import com.hokyozu.kyofuse.posts.repository.PostMapRepository;
import com.hokyozu.kyofuse.posts.repository.PostRepository;
import com.hokyozu.kyofuse.posts.validator.PostMapsValidator;
import com.hokyozu.kyofuse.posts.validator.PostValidator;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final GamerProfileRepository gamerProfileRepository;
    private final PostRepository postRepository;
    private final PostMapRepository postMapRepository;

    private final PostValidator postValidator;
    private final PostMapsValidator postMapsValidator;

    @Transactional
    public PostResponse post(UUID userId, CreatePostRequest request) {
        GamerProfile profile = gamerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Gamer profile not found for user ID: " + userId));

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
}
