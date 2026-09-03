package com.hokyozu.kyofuse.posts.repository;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.relationships.privacy.enums.ProfileVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = COALESCE(p.viewCount, 0) + :increment WHERE p.id = :postId")
    int incrementViewCount(@Param("postId") UUID postId, @Param("increment") long increment);

    // "CommunityIsNull" em todas as listagens fora da comunidade: post de comunidade é
    // exclusivo dela e nunca deve vazar pro feed geral nem pro perfil do autor — em
    // especial os PRIVATE, visíveis só a membros. Ver doc.md 10.5.
    @EntityGraph(attributePaths = {"author", "community"})
    Page<Post> findByVisibilityAndStatusAndCommunityIsNull(PostVisibility postVisibility, PostStatus postStatus, Pageable pageable);

    /**
     * Feed geral/anônimo, sem contexto de relacionamento por linha: só entra quem
     * deixou postsVisibility PUBLIC. FOLLOWERS/FRIENDS/PRIVATE não aparecem aqui —
     * só no perfil do autor ou no feed de seguindo, onde dá pra checar a relação.
     *
     * A exceção é o próprio usuário: privacidade protege dos outros, não de si mesmo.
     * Sem isso, quem deixasse postsVisibility diferente de PUBLIC sumia do próprio feed.
     */
    @EntityGraph(attributePaths = {"author", "community"})
    @Query("""
        select p from Post p
        where p.visibility = :postVisibility
            and p.status = :postStatus
            and p.community is null
            and (
                p.author.id = :viewerId
                or p.author.id in (
                    select ups.id from UserPrivacySettings ups where ups.profileVisibility = :profileVisibility
                )
            )
    """)
    Page<Post> findGlobalFeed(PostVisibility postVisibility, PostStatus postStatus, ProfileVisibility profileVisibility, UUID viewerId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "community"})
    Page<Post> findByAuthorIdAndVisibilityInAndStatusAndCommunityIsNull(UUID profileId, List<PostVisibility> postVisibility, PostStatus postStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "community"})
    Page<Post> findByAuthorIdAndVisibilityInAndStatusInAndCommunityIsNull(UUID userId, List<PostVisibility> postVisibility, List<PostStatus> statuses, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "community"})
    Page<Post> findByAuthorIdInAndVisibilityAndStatusAndCommunityIsNull(List<UUID> authorIds, PostVisibility postVisibility, PostStatus postStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "community"})
    Page<Post> findByCommunityIdAndVisibilityInAndStatus(UUID communityId, List<PostVisibility> postVisibility, PostStatus postStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "community"})
    @Query("""
        select p from Post p
        where p.author.id = :profileId
            and p.visibility in :visibilities
            and p.status = :status
            and p.community is null
            and exists (
                select 1 from PostMedia pm where pm.post = p
            )
    """)
    Page<Post> findMediaPostsByAuthorIdAndVisibilityInAndStatus(UUID profileId, List<PostVisibility> visibilities, PostStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "community"})
    @Query("""
        select p from Post p
        where p.author.id = :authorId
            and p.visibility in :visibilities
            and p.status in :statuses
            and p.community is null
            and exists (
                select 1 from PostMedia pm where pm.post = p
            )
    """)
    Page<Post> findMyMediaPosts(UUID authorId, List<PostVisibility> visibilities, List<PostStatus> statuses, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "community"})
    @Query("""
        select p from Post p
        where p.id = :postId
            and p.status = :activeStatus
            and (
                p.visibility = :publicVisibility
                or p.author.id = :requesterUserId
            )
""")
    Optional<Post> findVisiblePostForUser(UUID postId, UUID requesterUserId, PostStatus activeStatus, PostVisibility publicVisibility);

    @EntityGraph(attributePaths = {"author", "community"})
    Optional<Post> findByIdAndStatus(UUID id, PostStatus postStatus);
}
