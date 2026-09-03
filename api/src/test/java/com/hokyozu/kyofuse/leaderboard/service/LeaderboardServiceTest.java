package com.hokyozu.kyofuse.leaderboard.service;

import com.hokyozu.kyofuse.leaderboard.dto.response.LeaderboardEntryResponse;
import com.hokyozu.kyofuse.leaderboard.dto.response.UserRankResponse;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private GamerProfileFinder gamerProfileFinder;

    @Mock
    private com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository gamerProfileRepository;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private LeaderboardService leaderboardService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    void updateScoreAddsUserScoreToRedis() {
        UUID userId = UUID.randomUUID();

        leaderboardService.updateScore(userId, 18500.0);

        verify(zSetOperations).add("leaderboard:premier", userId.toString(), 18500.0);
    }

    @Test
    void getTopPlayersReturnsOrderedListWithProfileDetails() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>(user1.toString(), 22000.0));
        tuples.add(new DefaultTypedTuple<>(user2.toString(), 19500.0));

        when(zSetOperations.reverseRangeWithScores("leaderboard:premier", 0, 9)).thenReturn(tuples);

        GamerProfile p1 = GamerProfile.builder().user(User.builder().id(user1).build()).nickname("FalleN").country("BR").avatarUrl("fallen.png").build();
        GamerProfile p2 = GamerProfile.builder().user(User.builder().id(user2).build()).nickname("coldzera").country("BR").avatarUrl("cold.png").build();

        when(gamerProfileFinder.findAllByUserIds(List.of(user1, user2))).thenReturn(List.of(p1, p2));

        List<LeaderboardEntryResponse> result = leaderboardService.getTopPlayers(10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).rank()).isEqualTo(1L);
        assertThat(result.get(0).nickname()).isEqualTo("FalleN");
        assertThat(result.get(0).score()).isEqualTo(22000L);

        assertThat(result.get(1).rank()).isEqualTo(2L);
        assertThat(result.get(1).nickname()).isEqualTo("coldzera");
        assertThat(result.get(1).score()).isEqualTo(19500L);
    }

    @Test
    void getUserRankReturnsUserRankAndScore() {
        UUID userId = UUID.randomUUID();

        when(zSetOperations.reverseRank("leaderboard:premier", userId.toString())).thenReturn(41L);
        when(zSetOperations.score("leaderboard:premier", userId.toString())).thenReturn(16500.0);
        when(zSetOperations.zCard("leaderboard:premier")).thenReturn(5000L);

        UserRankResponse response = leaderboardService.getUserRank(userId);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.rank()).isEqualTo(42L);
        assertThat(response.score()).isEqualTo(16500L);
        assertThat(response.totalPlayers()).isEqualTo(5000L);
    }

    @Test
    void getAroundUserReturnsSurroundingPlayers() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        when(zSetOperations.reverseRank("leaderboard:premier", user1.toString())).thenReturn(5L);

        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>(user1.toString(), 20000.0));
        tuples.add(new DefaultTypedTuple<>(user2.toString(), 19000.0));

        when(zSetOperations.reverseRangeWithScores("leaderboard:premier", 3, 7)).thenReturn(tuples);

        GamerProfile p1 = GamerProfile.builder().user(User.builder().id(user1).build()).nickname("Player1").build();
        GamerProfile p2 = GamerProfile.builder().user(User.builder().id(user2).build()).nickname("Player2").build();
        when(gamerProfileFinder.findAllByUserIds(List.of(user1, user2))).thenReturn(List.of(p1, p2));

        List<LeaderboardEntryResponse> result = leaderboardService.getAroundUser(user1, 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).rank()).isEqualTo(4L);
    }

    @Test
    void syncLeaderboardFromDatabasePopulatesRedis() {
        UUID user1 = UUID.randomUUID();
        GamerProfile profile = GamerProfile.builder()
                .user(User.builder().id(user1).build())
                .premierRating(15000)
                .build();
        when(gamerProfileRepository.findAllWithPremierRatingAndActiveUser()).thenReturn(List.of(profile));

        leaderboardService.syncLeaderboardFromDatabase();

        verify(zSetOperations).add(eq("leaderboard:premier"), anySet());
    }

    @Test
    void removePlayerRemovesFromRedis() {
        UUID userId = UUID.randomUUID();

        leaderboardService.removePlayer(userId);

        verify(zSetOperations).remove("leaderboard:premier", userId.toString());
    }
}
