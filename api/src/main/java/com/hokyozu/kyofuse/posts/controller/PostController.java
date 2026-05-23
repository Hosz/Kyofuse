package com.hokyozu.kyofuse.posts.controller;

import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.dto.response.PostResponse;
import com.hokyozu.kyofuse.posts.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/post")
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse post(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid CreatePostRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return postService.post(userId, request);
    }
}
