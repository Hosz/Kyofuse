package com.hokyozu.kyofuse.posts.controller;

import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.dto.response.PostResponse;
import com.hokyozu.kyofuse.posts.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
@Validated
public class PostController {

    private final PostService postService;

    @PostMapping("/post")
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse post(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid CreatePostRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return postService.post(userId, request);
    }

    @GetMapping("/post/{postId}")
    public PostResponse getPost(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID postId) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return postService.getPost(userId, postId);
    }

    @GetMapping("/posts")
    public Page<PostResponse> getFeed(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Pageable pageable = defaultPageable(page, size);

        return postService.getFeed(pageable);
    }

    @GetMapping("/profile/{profileId}/posts")
    public Page<PostResponse> getProfilePosts(@PathVariable UUID profileId,
                                              @RequestParam(defaultValue = "0") @Min(0) int page,
                                              @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        Pageable pageable = defaultPageable(page, size);

        return postService.getProfilePosts(profileId, pageable);
    }

    @GetMapping("/posts/me")
    public Page<PostResponse> getMyPosts(@AuthenticationPrincipal Jwt jwt,
                                         @RequestParam(defaultValue = "0") @Min(0) int page,
                                         @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Pageable pageable = defaultPageable(page, size);

        return postService.getMyPosts(userId, pageable);
    }

    private Pageable defaultPageable(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
