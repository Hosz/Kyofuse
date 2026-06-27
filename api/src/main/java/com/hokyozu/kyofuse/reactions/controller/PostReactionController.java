package com.hokyozu.kyofuse.reactions.controller;

import com.hokyozu.kyofuse.reactions.dto.request.PostReactionRequest;
import com.hokyozu.kyofuse.reactions.dto.response.PostReactionResponse;
import com.hokyozu.kyofuse.reactions.service.PostReactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/posts/reactions")
@RequiredArgsConstructor
public class PostReactionController {

    private final PostReactionService postReactionService;

    @PostMapping("/{postId}")
    public PostReactionResponse upsertReaction(@PathVariable UUID postId, @RequestBody @Valid PostReactionRequest request, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return postReactionService.upsertReaction(userId, postId, request);
    }
}
