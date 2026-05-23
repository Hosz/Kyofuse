package com.hokyozu.kyofuse.posts.repository;

import com.hokyozu.kyofuse.posts.entity.PostMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostMapRepository extends JpaRepository<PostMap, UUID> {
    List<PostMap> findByPostId(UUID postId);
}
