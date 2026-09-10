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
    public static final String POST_VIEWS_SEEN_PREFIX = "post:views:seen:";

    public boolean recordView(UUID postId, UUID userId) {
        if (userId == null) {
            recordView(postId);
            return true;
        }

        String seenKey = POST_VIEWS_SEEN_PREFIX + postId;
        Long added = redisTemplate.opsForSet().add(seenKey, userId.toString());
        if (added != null && added > 0) {
            recordView(postId);
            return true;
        }
        return false;
    }

    public void recordView(UUID postId) {
        String key = POST_VIEWS_BUFFER_PREFIX + postId;
        redisTemplate.opsForValue().increment(key);
    }

    public boolean hasUserViewed(UUID postId, UUID userId) {
        if (userId == null) {
            return false;
        }
        Boolean isMember = redisTemplate.opsForSet().isMember(POST_VIEWS_SEEN_PREFIX + postId, userId.toString());
        return Boolean.TRUE.equals(isMember);
    }
}
