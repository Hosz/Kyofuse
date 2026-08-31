package com.hokyozu.kyofuse.posts.mapper;

import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.dto.response.PostMediaResponse;
import com.hokyozu.kyofuse.posts.dto.response.PostResponse;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.entity.PostMap;
import com.hokyozu.kyofuse.posts.entity.PostMedia;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;
import java.util.List;

public class PostMapper {

    public static Post toEntity(User user, CreatePostRequest request) {
        return toEntity(user, request, null);
    }

    /** community null = post normal; preenchido = post exclusivo daquela comunidade. */
    public static Post toEntity(User user, CreatePostRequest request, Community community) {
        return Post.builder()
                .author(user)
                .community(community)
                .content(request.content() != null ? request.content() : "")
                .postType(request.postType())
                .visibility(request.visibility())
                .status(PostStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static PostResponse toResponse(Post savedPost, List<PostMap> postMaps, GamerProfile profile) {
        return toResponse(savedPost, postMaps, List.of(), profile);
    }

    public static PostResponse toResponse(Post savedPost, List<PostMap> postMaps, List<PostMedia> postMedia, GamerProfile profile) {
        List<String> maps = postMaps == null
                ? List.of()
                : postMaps.stream()
                        .map(PostMap::getMapName)
                        .toList();

        List<PostMediaResponse> mediaResponses = postMedia == null
                ? List.of()
                : postMedia.stream()
                        .map(PostMediaMapper::toResponse)
                        .toList();

        return new PostResponse(
                savedPost.getId(),
                savedPost.getAuthor().getId(),
                profile != null ? profile.getNickname() : null,
                savedPost.getAuthor().getUsername(),
                profile != null ? profile.getAvatarUrl() : null,
                savedPost.getCommunity() != null ? savedPost.getCommunity().getId() : null,
                savedPost.getCommunity() != null ? savedPost.getCommunity().getName() : null,
                savedPost.getContent(),
                savedPost.getPostType(),
                savedPost.getVisibility(),
                savedPost.getStatus(),
                savedPost.getReactionCount(),
                savedPost.getLikeCount(),
                savedPost.getCommentCount(),
                maps,
                mediaResponses,
                savedPost.getCreatedAt(),
                savedPost.getUpdatedAt()
        );
    }

    public static List<PostMap> toPostMap(Post post, List<Cs2Map> maps) {
        if (maps == null || maps.isEmpty()) {
            return List.of();
        }

        return maps.stream()
                .map(map -> PostMap.builder()
                        .post(post)
                        .mapName(map.name())
                        .createdAt(Instant.now())
                        .build())
                .toList();
    }
}
