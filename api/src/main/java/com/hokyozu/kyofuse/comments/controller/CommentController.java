package com.hokyozu.kyofuse.comments.controller;

import com.hokyozu.kyofuse.comments.dto.request.CreateCommentRequest;
import com.hokyozu.kyofuse.comments.dto.response.CommentResponse;
import com.hokyozu.kyofuse.comments.service.CommentService;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/post/{postId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse postComment(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable UUID postId,
                                       @RequestBody @Valid CreateCommentRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return commentService.postComment(userId, postId, request);
    }

    @GetMapping("/{commentId}")
    public CommentResponse getComment(@PathVariable UUID commentId) {

        return commentService.getComment(commentId);
    }

    @GetMapping("/post/{postId}/comments")
    public Page<CommentResponse> listComments(@PathVariable UUID postId, Pageable pageable) {

        return commentService.listComments(postId, pageable);
    }

    @PatchMapping("/{postId}/{commentId}/delete")
    public void deleteComment(@PathVariable UUID commentId, @PathVariable UUID postId, @AuthenticationPrincipal Jwt jwt) {
        UUID user = UUID.fromString(jwt.getSubject());

        commentService.deleteComment(commentId, postId, user);
    }
}
