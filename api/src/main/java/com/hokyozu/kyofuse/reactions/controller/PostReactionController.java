package com.hokyozu.kyofuse.reactions.controller;

import com.hokyozu.kyofuse.reactions.dto.request.PostReactionRequest;
import com.hokyozu.kyofuse.reactions.dto.response.PostReactionResponse;
import com.hokyozu.kyofuse.reactions.service.PostReactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts/reactions")
@RequiredArgsConstructor
public class PostReactionController {

    private final PostReactionService postReactionService;

    @PostMapping("/{postId}")
    public PostReactionResponse upsertReaction(@PathVariable UUID postId, @RequestBody @Valid PostReactionRequest request, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return postReactionService.upsertReaction(userId, postId, request);
    }

    @DeleteMapping("/{postId}/remove")
    public void removeReaction(@PathVariable UUID postId, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        postReactionService.removeReaction(userId, postId);
    }

    @GetMapping("/{postId}/reactions")
    public Page<PostReactionResponse> getReactions(@PathVariable UUID postId, @AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return postReactionService.getReactions(userId, postId, pageable);
    }

    @GetMapping("/{postId}/likes")
    public Page<PostReactionResponse> getLikes(@PathVariable UUID postId, @AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return postReactionService.getLikes(userId, postId, pageable);
    }
}
