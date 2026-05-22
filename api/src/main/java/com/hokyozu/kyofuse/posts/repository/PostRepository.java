package com.hokyozu.kyofuse.posts.repository;

import com.hokyozu.kyofuse.posts.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
}
