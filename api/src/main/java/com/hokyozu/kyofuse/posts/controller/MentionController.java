package com.hokyozu.kyofuse.posts.controller;

import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.teams.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/mentions")
@RequiredArgsConstructor
public class MentionController {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final CommunityRepository communityRepository;

    @GetMapping("/{slug}/resolve")
    public Map<String, String> resolveMention(@PathVariable String slug) {
        if (userRepository.findByUsername(slug).isPresent()) {
            return Map.of("type", "USER", "url", "/perfil/" + slug);
        }
        if (teamRepository.findBySlug(slug).isPresent()) {
            return Map.of("type", "TEAM", "url", "/times/" + slug); // assuming /times/:slug
        }
        if (communityRepository.findBySlug(slug).isPresent()) {
            return Map.of("type", "COMMUNITY", "url", "/comunidade/" + slug); // assuming /comunidade/:slug
        }
        return Map.of("type", "UNKNOWN", "url", "/perfil/" + slug);
    }
}
