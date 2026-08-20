package com.hokyozu.kyofuse.posts.repository;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.relationships.privacy.enums.ProfileVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    // "CommunityIsNull" em todas as listagens fora da comunidade: post de comunidade é
    // exclusivo dela e nunca deve vazar pro feed geral nem pro perfil do autor — em
    // especial os PRIVATE, visíveis só a membros. Ver doc.md 10.5.
    Page<Post> findByVisibilityAndStatusAndCommunityIsNull(PostVisibility postVisibility, PostStatus postStatus, Pageable pageable);

    /**
     * Feed geral/anônimo, sem contexto de relacionamento por linha: só entra quem
     * deixou postsVisibility PUBLIC. FOLLOWERS/FRIENDS/PRIVATE não aparecem aqui —
     * só no perfil do autor ou no feed de seguindo, onde dá pra checar a relação.
     *
     * A exceção é o próprio usuário: privacidade protege dos outros, não de si mesmo.
     * Sem isso, quem deixasse postsVisibility diferente de PUBLIC sumia do próprio feed.
     */
    @Query("""
        select p from Post p
        where p.visibility = :postVisibility
            and p.status = :postStatus
            and p.community is null
            and (
                p.author.id = :viewerId
                or p.author.id in (
                    select ups.id from UserPrivacySettings ups where ups.postsVisibility = :postsVisibility
                )
            )
    """)
    Page<Post> findGlobalFeed(PostVisibility postVisibility, PostStatus postStatus, ProfileVisibility postsVisibility, UUID viewerId, Pageable pageable);

    Page<Post> findByAuthorIdAndVisibilityInAndStatusAndCommunityIsNull(UUID profileId, List<PostVisibility> postVisibility, PostStatus postStatus, Pageable pageable);

    Page<Post> findByAuthorIdAndVisibilityInAndStatusInAndCommunityIsNull(UUID userId, List<PostVisibility> postVisibility, List<PostStatus> statuses, Pageable pageable);

    Page<Post> findByAuthorIdInAndVisibilityAndStatusAndCommunityIsNull(List<UUID> authorIds, PostVisibility postVisibility, PostStatus postStatus, Pageable pageable);

    Page<Post> findByCommunityIdAndVisibilityInAndStatus(UUID communityId, List<PostVisibility> postVisibility, PostStatus postStatus, Pageable pageable);

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

    Optional<Post> findByIdAndStatus(UUID id, PostStatus postStatus);
}
