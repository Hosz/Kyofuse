package com.hokyozu.kyofuse.posts.repository;

import com.hokyozu.kyofuse.posts.entity.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostMediaRepository extends JpaRepository<PostMedia, UUID> {
    List<PostMedia> findByPostIdOrderByDisplayOrderAsc(UUID postId);

    List<PostMedia> findByPostIdInOrderByDisplayOrderAsc(List<UUID> postIds);
}
