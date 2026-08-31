package com.hokyozu.kyofuse.posts.service;

import com.hokyozu.kyofuse.posts.dto.response.PostMediaResponse;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.entity.PostMedia;
import com.hokyozu.kyofuse.posts.mapper.PostMediaMapper;
import com.hokyozu.kyofuse.posts.repository.PostMediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostMediaService {

    private final PostMediaRepository postMediaRepository;

    @Transactional
    public PostMedia attachMediaToPost(
            Post post,
            String fileKey,
            String url,
            String thumbnailUrl,
            String contentType,
            Long fileSizeBytes,
            Integer width,
            Integer height,
            Integer displayOrder
    ) {
        PostMedia postMedia = PostMedia.builder()
                .post(post)
                .fileKey(fileKey)
                .url(url)
                .thumbnailUrl(thumbnailUrl)
                .contentType(contentType)
                .fileSizeBytes(fileSizeBytes)
                .width(width)
                .height(height)
                .displayOrder(displayOrder != null ? displayOrder : 0)
                .createdAt(Instant.now())
                .build();

        return postMediaRepository.save(postMedia);
    }

    @Transactional(readOnly = true)
    public List<PostMediaResponse> findByPostId(UUID postId) {
        return postMediaRepository.findByPostIdOrderByDisplayOrderAsc(postId)
                .stream()
                .map(PostMediaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostMedia> getEntitiesByPostId(UUID postId) {
        return postMediaRepository.findByPostIdOrderByDisplayOrderAsc(postId);
    }

    @Transactional(readOnly = true)
    public List<PostMedia> getEntitiesByPostIds(List<UUID> postIds) {
        return postMediaRepository.findByPostIdInOrderByDisplayOrderAsc(postIds);
    }
}
