package com.hokyozu.kyofuse.leaderboard.controller;

import com.hokyozu.kyofuse.leaderboard.dto.response.LeaderboardEntryResponse;
import com.hokyozu.kyofuse.leaderboard.dto.response.UserRankResponse;
import com.hokyozu.kyofuse.leaderboard.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/top")
    public ResponseEntity<List<LeaderboardEntryResponse>> getTopPlayers(
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(leaderboardService.getTopPlayers(limit));
    }

    @GetMapping("/rank/me")
    public ResponseEntity<UserRankResponse> getMyRank(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(leaderboardService.getUserRank(userId));
    }

    @GetMapping("/rank/{userId}")
    public ResponseEntity<UserRankResponse> getUserRank(@PathVariable UUID userId) {
        return ResponseEntity.ok(leaderboardService.getUserRank(userId));
    }

    @GetMapping("/around/me")
    public ResponseEntity<List<LeaderboardEntryResponse>> getAroundMe(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "3") int range
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(leaderboardService.getAroundUser(userId, range));
    }
}
