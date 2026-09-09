package com.hokyozu.kyofuse.posts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostViewsBufferServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private PostViewsBufferService postViewsBufferService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    void recordViewIncrementsBufferKey() {
        UUID postId = UUID.randomUUID();

        postViewsBufferService.recordView(postId);

        verify(valueOperations).increment("post:views:buffer:" + postId);
    }

    @Test
    void recordViewWithUserIdIncrementsBufferOnlyOnFirstView() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String seenKey = "post:views:seen:" + postId;

        // First view: user was not in the set
        when(setOperations.add(seenKey, userId.toString())).thenReturn(1L);

        boolean firstRecorded = postViewsBufferService.recordView(postId, userId);

        assertThat(firstRecorded).isTrue();
        verify(setOperations).add(seenKey, userId.toString());
        verify(valueOperations).increment("post:views:buffer:" + postId);

        // Second view: user is already in the set
        when(setOperations.add(seenKey, userId.toString())).thenReturn(0L);

        boolean secondRecorded = postViewsBufferService.recordView(postId, userId);

        assertThat(secondRecorded).isFalse();
        // Buffer should NOT have been incremented a second time
        verify(valueOperations, times(1)).increment("post:views:buffer:" + postId);
    }

    @Test
    void recordViewWithNullUserIdAlwaysIncrements() {
        UUID postId = UUID.randomUUID();

        boolean recorded = postViewsBufferService.recordView(postId, null);

        assertThat(recorded).isTrue();
        verify(valueOperations).increment("post:views:buffer:" + postId);
    }
}
