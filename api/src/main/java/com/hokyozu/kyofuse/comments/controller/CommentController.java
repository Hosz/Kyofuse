package com.hokyozu.kyofuse.comments.controller;

import com.hokyozu.kyofuse.comments.dto.request.CreateCommentRequest;
import com.hokyozu.kyofuse.comments.dto.response.CommentResponse;
import com.hokyozu.kyofuse.comments.service.CommentService;
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

    @com.hokyozu.kyofuse.infrastructure.ratelimit.RateLimit(key = "create_comment", limit = 20, period = 60, type = com.hokyozu.kyofuse.infrastructure.ratelimit.RateLimitType.USER_ID)
    @PostMapping("/post/{postId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse postComment(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable UUID postId,
                                       @RequestBody @Valid CreateCommentRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return commentService.postComment(userId, postId, request);
    }

    @GetMapping("/{commentId}")
    public CommentResponse getComment(@AuthenticationPrincipal Jwt jwt,
                                      @PathVariable UUID commentId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return commentService.getComment(commentId, userId);
    }

    @GetMapping("/post/{postId}/comments")
    public Page<CommentResponse> listComments(@AuthenticationPrincipal Jwt jwt,
                                              @PathVariable UUID postId,
                                              Pageable pageable) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return commentService.listComments(postId, pageable, userId);
    }

    @GetMapping("/user/{userId}/comments")
    public Page<CommentResponse> listUserComments(@AuthenticationPrincipal Jwt jwt,
                                                   @PathVariable UUID userId,
                                                   Pageable pageable) {
        UUID viewerId = UUID.fromString(jwt.getSubject());
        return commentService.listUserComments(viewerId, userId, pageable);
    }

    @RequestMapping(
            value = {"/{postId}/{commentId}/delete", "/{postId}/{commentId}"},
            method = {RequestMethod.DELETE, RequestMethod.PATCH}
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable("commentId") UUID commentId,
                              @PathVariable("postId") UUID postId,
                              @AuthenticationPrincipal Jwt jwt) {
        UUID user = UUID.fromString(jwt.getSubject());

        commentService.deleteComment(commentId, postId, user);
    }
}
