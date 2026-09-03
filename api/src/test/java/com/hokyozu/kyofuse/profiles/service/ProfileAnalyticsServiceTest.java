package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.profiles.dto.response.ProfileAnalyticsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HyperLogLogOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileAnalyticsServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HyperLogLogOperations<String, String> hyperLogLogOperations;

    @InjectMocks
    private ProfileAnalyticsService profileAnalyticsService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForHyperLogLog()).thenReturn(hyperLogLogOperations);
    }

    @Test
    void recordProfileVisitAddsToDailyAndMonthlyHyperLogLog() {
        UUID profileId = UUID.randomUUID();
        UUID viewerId = UUID.randomUUID();

        profileAnalyticsService.recordProfileVisit(profileId, viewerId);

        verify(hyperLogLogOperations, times(2)).add(anyString(), eq(viewerId.toString()));
        verify(redisTemplate, times(2)).expire(anyString(), any(Duration.class));
    }

    @Test
    void recordProfileVisitIgnoresSelfVisit() {
        UUID profileId = UUID.randomUUID();

        profileAnalyticsService.recordProfileVisit(profileId, profileId);

        verify(hyperLogLogOperations, never()).add(anyString(), anyString());
    }

    @Test
    void getAnalyticsReturnsDailyAndMonthlyCounts() {
        UUID profileId = UUID.randomUUID();

        when(hyperLogLogOperations.size(anyString())).thenReturn(45L).thenReturn(320L);

        ProfileAnalyticsResponse response = profileAnalyticsService.getAnalytics(profileId);

        assertThat(response.profileId()).isEqualTo(profileId);
        assertThat(response.dailyUniqueVisitors()).isEqualTo(45L);
        assertThat(response.monthlyUniqueVisitors()).isEqualTo(320L);
    }
}
