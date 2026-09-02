package com.hokyozu.kyofuse.posts.controller;

import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.teams.repository.TeamRepository;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mentions/search")
@RequiredArgsConstructor
public class MentionSearchController {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final CommunityRepository communityRepository;
    private final GamerProfileRepository gamerProfileRepository;

    @GetMapping
    public List<MentionSearchResult> searchMentions(@RequestParam String prefix, @RequestParam String query) {
        if ("@".equals(prefix)) {
            return gamerProfileRepository.searchActiveProfilesForMention(query, UserStatus.ACTIVE, PageRequest.of(0, 5)).stream()
                    .map(gp -> new MentionSearchResult(
                            "USER",
                            gp.getUser().getUsername(),
                            gp.getNickname() != null && !gp.getNickname().isBlank() ? gp.getNickname() : gp.getUser().getUsername(),
                            gp.getAvatarUrl() != null && !gp.getAvatarUrl().isBlank() ? gp.getAvatarUrl() : "/assets/profile/profile-image-default.png"
                    ))
                    .toList();
        } else if ("$".equals(prefix)) {
            return teamRepository.searchActiveTeams(query, List.of(TeamStatus.ACTIVE, TeamStatus.RECRUITING), PageRequest.of(0, 5)).stream()
                    .map(t -> new MentionSearchResult("TEAM", t.getSlug(), t.getName(), t.getAvatarUrl() != null ? t.getAvatarUrl() : "/assets/profile/team-profile-image-default.png"))
                    .toList();
        } else if ("//".equals(prefix)) {
            return communityRepository.searchActiveCommunities(query, CommunityStatus.ACTIVE, PageRequest.of(0, 5)).stream()
                    .map(c -> new MentionSearchResult("COMMUNITY", c.getSlug(), c.getName(), c.getAvatarUrl() != null ? c.getAvatarUrl() : "/assets/profile/community-profile-image-default.png"))
                    .toList();
        }
        return List.of();
    }

    public record MentionSearchResult(String type, String slug, String name, String avatarUrl) {}
}
