package com.hokyozu.kyofuse.posts.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostViewsBufferService {

    private final StringRedisTemplate redisTemplate;

    public static final String POST_VIEWS_BUFFER_PREFIX = "post:views:buffer:";

    public void recordView(UUID postId) {
        String key = POST_VIEWS_BUFFER_PREFIX + postId;
        redisTemplate.opsForValue().increment(key);
    }
}
