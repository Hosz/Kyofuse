package com.hokyozu.kyofuse.profiles.service;

import com.hokyozu.kyofuse.profiles.dto.response.ProfileAnalyticsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileAnalyticsService {

    private final StringRedisTemplate redisTemplate;

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    public void recordProfileVisit(UUID profileId, UUID viewerId) {
        if (profileId == null || viewerId == null || profileId.equals(viewerId)) {
            return;
        }

        LocalDate now = LocalDate.now();
        String dayKey = "profile:visitors:daily:" + profileId + ":" + now.format(DAY_FORMATTER);
        String monthKey = "profile:visitors:monthly:" + profileId + ":" + now.format(MONTH_FORMATTER);

        try {
            redisTemplate.opsForHyperLogLog().add(dayKey, viewerId.toString());
            redisTemplate.expire(dayKey, Duration.ofDays(35));

            redisTemplate.opsForHyperLogLog().add(monthKey, viewerId.toString());
            redisTemplate.expire(monthKey, Duration.ofDays(365));
        } catch (Exception e) {
            log.warn("Erro ao registrar visita no perfil {}: {}", profileId, e.getMessage());
        }
    }

    public ProfileAnalyticsResponse getAnalytics(UUID profileId) {
        LocalDate now = LocalDate.now();
        String dayKey = "profile:visitors:daily:" + profileId + ":" + now.format(DAY_FORMATTER);
        String monthKey = "profile:visitors:monthly:" + profileId + ":" + now.format(MONTH_FORMATTER);

        Long dailyCount = redisTemplate.opsForHyperLogLog().size(dayKey);
        Long monthlyCount = redisTemplate.opsForHyperLogLog().size(monthKey);

        return new ProfileAnalyticsResponse(
                profileId,
                dailyCount != null ? dailyCount : 0L,
                monthlyCount != null ? monthlyCount : 0L
        );
    }
}
