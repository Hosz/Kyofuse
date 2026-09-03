package com.hokyozu.kyofuse.posts.scheduler;

import com.hokyozu.kyofuse.posts.repository.PostRepository;
import com.hokyozu.kyofuse.posts.service.PostViewsBufferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostViewsFlushSchedulerTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private PostViewsFlushScheduler postViewsFlushScheduler;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void flushViewsToDatabaseFlushesAllBufferedKeys() {
        UUID post1 = UUID.randomUUID();
        UUID post2 = UUID.randomUUID();
        String key1 = PostViewsBufferService.POST_VIEWS_BUFFER_PREFIX + post1;
        String key2 = PostViewsBufferService.POST_VIEWS_BUFFER_PREFIX + post2;

        when(redisTemplate.keys(PostViewsBufferService.POST_VIEWS_BUFFER_PREFIX + "*"))
                .thenReturn(Set.of(key1, key2));
        when(valueOperations.get(key1)).thenReturn("15");
        when(valueOperations.get(key2)).thenReturn("8");

        postViewsFlushScheduler.flushViewsToDatabase();

        verify(postRepository).incrementViewCount(post1, 15L);
        verify(postRepository).incrementViewCount(post2, 8L);
        verify(redisTemplate).delete(key1);
        verify(redisTemplate).delete(key2);
    }

    @Test
    void flushViewsToDatabaseDoesNothingWhenNoKeys() {
        when(redisTemplate.keys(PostViewsBufferService.POST_VIEWS_BUFFER_PREFIX + "*"))
                .thenReturn(Set.of());

        postViewsFlushScheduler.flushViewsToDatabase();

        verify(postRepository, never()).incrementViewCount(any(), anyLong());
    }
}
