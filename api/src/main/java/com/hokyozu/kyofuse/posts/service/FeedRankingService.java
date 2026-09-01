package com.hokyozu.kyofuse.posts.service;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.entity.PostMap;
import com.hokyozu.kyofuse.posts.enums.PostType;
import com.hokyozu.kyofuse.posts.repository.PostMapRepository;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.entity.GamerProfileFavoriteMap;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileFavoriteMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedRankingService {

    private final GamerProfileFinder gamerProfileFinder;
    private final GamerProfileFavoriteMapRepository favoriteMapRepository;
    private final PostMapRepository postMapRepository;

    public List<Post> rankPosts(List<Post> candidates, GamerProfile viewerProfile) {
        if (candidates.isEmpty()) {
            return candidates;
        }

        // 1. Fetch related data in batch to avoid N+1
        List<UUID> authorIds = candidates.stream().map(post -> post.getAuthor().getId()).distinct().toList();
        List<UUID> postIds = candidates.stream().map(Post::getId).toList();

        Map<UUID, GamerProfile> authorProfiles = gamerProfileFinder.findAllByUserIds(authorIds).stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), profile -> profile));

        Map<UUID, List<PostMap>> mapsByPost = postMapRepository.findByPostIdIn(postIds).stream()
                .collect(Collectors.groupingBy(postMap -> postMap.getPost().getId()));

        Set<String> viewerMaps = Set.of();
        if (viewerProfile != null) {
            viewerMaps = favoriteMapRepository.findByProfile_IdIn(List.of(viewerProfile.getId()))
                    .stream()
                    .map(m -> m.getMapName().name())
                    .collect(Collectors.toSet());
        }

        final Set<String> finalViewerMaps = viewerMaps;

        // 2. Score and sort
        List<PostScore> scoredPosts = new ArrayList<>();
        for (Post post : candidates) {
            GamerProfile authorProfile = authorProfiles.get(post.getAuthor().getId());
            List<PostMap> postMaps = mapsByPost.getOrDefault(post.getId(), List.of());
            
            double score = calculateScore(post, postMaps, authorProfile, viewerProfile, finalViewerMaps);
            scoredPosts.add(new PostScore(post, score));
        }

        // Sort descending
        scoredPosts.sort((a, b) -> Double.compare(b.score(), a.score()));

        return scoredPosts.stream().map(PostScore::post).toList();
    }

    private double calculateScore(Post post, List<PostMap> postMaps, GamerProfile authorProfile, GamerProfile viewerProfile, Set<String> viewerMaps) {
        double score = 0.0;

        // --- Pilar 1: Engajamento Social ---
        score += (post.getCommentCount() != null ? post.getCommentCount() : 0) * 5.0;
        score += (post.getReactionCount() != null ? post.getReactionCount() : 0) * 2.0;

        if (post.getPostType() == PostType.HIGHLIGHT || post.getPostType() == PostType.LINEUP_TIP) {
            score += 15.0;
        }

        // --- Pilar 2: Afinidade (se viewer autenticado) ---
        if (viewerProfile != null) {
            // Match de mapas
            boolean mapMatch = postMaps.stream().anyMatch(m -> viewerMaps.contains(m.getMapName()));
            if (mapMatch) {
                score += 30.0;
            }

            // Match de Rank / Rating (Premier)
            if (viewerProfile.getPremierRating() != null && authorProfile != null && authorProfile.getPremierRating() != null) {
                int diff = Math.abs(viewerProfile.getPremierRating() - authorProfile.getPremierRating());
                if (diff <= 3000) {
                    score += 25.0;
                }
            }
            
            // Match de Nivel Faceit
            if (viewerProfile.getFaceitLevel() != null && authorProfile != null && authorProfile.getFaceitLevel() != null) {
                int diff = Math.abs(viewerProfile.getFaceitLevel() - authorProfile.getFaceitLevel());
                if (diff <= 2) {
                    score += 15.0;
                }
            }

            // Match de LFT/LFD
            if ((post.getPostType() == PostType.LOOKING_FOR_DUO || post.getPostType() == PostType.LOOKING_FOR_TEAM)) {
                if (Boolean.TRUE.equals(viewerProfile.getLookingForDuo()) || Boolean.TRUE.equals(viewerProfile.getLookingForTeam())) {
                    score += 40.0;
                }
            }
        }

        // --- Pilar 3: Discovery Boost (Exploration) ---
        long hoursOld = Duration.between(post.getCreatedAt(), Instant.now()).toHours();
        if (hoursOld < 3) {
            score += 20.0;
        }

        // --- Pilar 4: Decaimento Temporal (Gravidade) ---
        // Formula: Score / (AgeHours + 2)^1.4
        double timeDecay = Math.pow(hoursOld + 2.0, 1.4);

        return score / timeDecay;
    }

    private record PostScore(Post post, double score) {}
}
