package com.hokyozu.kyofuse.comments.service;

import com.hokyozu.kyofuse.comments.dto.request.CreateCommentRequest;
import com.hokyozu.kyofuse.comments.dto.response.CommentResponse;
import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.comments.mapper.CommentMapper;
import com.hokyozu.kyofuse.comments.repository.CommentRepository;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.finder.PostFinder;
import com.hokyozu.kyofuse.posts.repository.PostRepository;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    private final PostFinder postFinder;
    private final GamerProfileFinder gamerProfileFinder;

    @Transactional
    public CommentResponse postComment(UUID userId, UUID postId, CreateCommentRequest request) {
        GamerProfile profile = gamerProfileFinder.findProfileByUserId(userId);

        Post post = postFinder.findPostByIdAndStatus(postId, PostStatus.ACTIVE);

        Comment comment = CommentMapper.toEntity(profile, request, post);
        Comment savedComment = commentRepository.save(comment);

        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        return CommentMapper.toResponse(savedComment);
    }
}
