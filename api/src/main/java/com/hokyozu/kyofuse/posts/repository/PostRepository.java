package com.hokyozu.kyofuse.posts.repository;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    Page<Post> findByVisibilityAndStatus(PostVisibility postVisibility, PostStatus postStatus, Pageable pageable);

    Page<Post> findByAuthorIdAndVisibilityInAndStatus(UUID profileId, List<PostVisibility> postVisibility, PostStatus postStatus, Pageable pageable);

    Page<Post> findByAuthorIdAndVisibilityInAndStatusIn(UUID userId, List<PostVisibility> postVisibility, List<PostStatus> statuses, Pageable pageable);

    @Query("""
        select p from Post p
        where p.id = :postId
            and p.status <> :deletedStatus
            and (
                p.visibility = :publicVisibility
                or p.author.id = :requesterUserId
            )
""")
    Optional<Post> findVisiblePostForUser(UUID postId, UUID requesterUserId, PostStatus deletedStatus, PostVisibility publicVisibility);

    Optional<Post> findByIdAndStatus(UUID id, PostStatus postStatus);
}
