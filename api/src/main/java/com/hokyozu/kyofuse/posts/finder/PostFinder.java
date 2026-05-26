package com.hokyozu.kyofuse.posts.finder;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.posts.repository.PostRepository;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostFinder {

    private final PostRepository postRepository;

    public Post findPostByIdAndStatus(UUID postId, PostStatus postStatus) {
        return postRepository.findByIdAndStatus(postId, postStatus)
                .orElseThrow(() -> new BadRequestException("Post not found for ID: " + postId));
    }

    public Post findVisiblePostForUser(UUID postId, UUID userId, PostStatus postStatus, PostVisibility postVisibility) {
        return postRepository.findVisiblePostForUser(postId, userId, postStatus, postVisibility)
                .orElseThrow(() -> new BadRequestException("Post not found for ID: " + postId));
    }

    public Post findById(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BadRequestException("Post not found for ID: " + postId));
    }
}
