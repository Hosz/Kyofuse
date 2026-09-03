package com.hokyozu.kyofuse.posts.repository;

import com.hokyozu.kyofuse.posts.entity.PostMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostMapRepository extends JpaRepository<PostMap, UUID> {
    List<PostMap> findByPostId(UUID postId);

    List<PostMap> findByPostIdIn(List<UUID> postIds);
}
