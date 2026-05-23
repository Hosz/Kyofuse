package com.hokyozu.kyofuse.posts.service;

import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.dto.response.PostResponse;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.mapper.PostMapper;
import com.hokyozu.kyofuse.posts.repository.PostRepository;
import com.hokyozu.kyofuse.posts.validator.PostValidator;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final GamerProfileRepository gamerProfileRepository;
    private final PostRepository postRepository;

    private final PostValidator postValidator;

    @Transactional
    public PostResponse post(UUID userId, CreatePostRequest request) {
        GamerProfile profile = gamerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Gamer profile not found for user ID: " + userId));

        postValidator.validate(request);

        Post post = PostMapper.toEntity(profile, request);
        Post savedPost = postRepository.save(post);
        return PostMapper.toResponse(savedPost);
    }
}
