package com.hokyozu.kyofuse.reactions.controller;

import com.hokyozu.kyofuse.reactions.dto.request.CommentReactionRequest;
import com.hokyozu.kyofuse.reactions.dto.response.CommentReactionResponse;
import com.hokyozu.kyofuse.reactions.service.CommentReactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/comments/reactions")
@RequiredArgsConstructor
public class CommentReactionController {

    private final CommentReactionService commentReactionService;

    @PostMapping("/{postId}/{commentId}")
    public CommentReactionResponse upsertReaction(@PathVariable UUID postId, @PathVariable UUID commentId, @RequestBody @Valid CommentReactionRequest request, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return commentReactionService.upsertReaction(postId, commentId, request, userId);
    }

    @DeleteMapping("/{postId}/{commentId}")
    public void removeReaction(@PathVariable UUID postId, @PathVariable UUID commentId, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        commentReactionService.removeReaction(userId, postId, commentId);
    }
}
