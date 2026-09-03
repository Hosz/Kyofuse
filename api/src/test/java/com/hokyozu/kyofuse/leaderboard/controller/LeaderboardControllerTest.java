package com.hokyozu.kyofuse.leaderboard.controller;

import com.hokyozu.kyofuse.leaderboard.dto.response.LeaderboardEntryResponse;
import com.hokyozu.kyofuse.leaderboard.dto.response.UserRankResponse;
import com.hokyozu.kyofuse.leaderboard.service.LeaderboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardControllerTest {

    @Mock
    private LeaderboardService leaderboardService;

    @InjectMocks
    private LeaderboardController leaderboardController;

    @Test
    void getTopPlayersReturnsList() {
        LeaderboardEntryResponse entry = new LeaderboardEntryResponse(1L, UUID.randomUUID(), "Player", "avatar.png", "BR", 20000L);
        when(leaderboardService.getTopPlayers(50)).thenReturn(List.of(entry));

        ResponseEntity<List<LeaderboardEntryResponse>> response = leaderboardController.getTopPlayers(50);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsExactly(entry);
    }

    @Test
    void getMyRankReturnsUserRank() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        UserRankResponse rank = new UserRankResponse(userId, 5L, 18000L, 1000L);
        when(leaderboardService.getUserRank(userId)).thenReturn(rank);

        ResponseEntity<UserRankResponse> response = leaderboardController.getMyRank(jwt);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(rank);
    }
}
