package com.hokyozu.kyofuse.posts.scheduler;

import com.hokyozu.kyofuse.posts.repository.PostRepository;
import com.hokyozu.kyofuse.posts.service.PostViewsBufferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostViewsFlushScheduler {

    private final StringRedisTemplate redisTemplate;
    private final PostRepository postRepository;

    @Scheduled(fixedDelay = 120000)
    @Transactional
    public void flushViewsToDatabase() {
        Set<String> keys = redisTemplate.keys(PostViewsBufferService.POST_VIEWS_BUFFER_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            try {
                String val = redisTemplate.opsForValue().get(key);
                if (val != null) {
                    long viewsIncrement = Long.parseLong(val);
                    String postIdStr = key.replace(PostViewsBufferService.POST_VIEWS_BUFFER_PREFIX, "");
                    UUID postId = UUID.fromString(postIdStr);

                    postRepository.incrementViewCount(postId, viewsIncrement);
                    redisTemplate.delete(key);
                }
            } catch (Exception e) {
                log.warn("Erro ao descarregar visualizações do post para a chave {}: {}", key, e.getMessage());
            }
        }
    }
}
