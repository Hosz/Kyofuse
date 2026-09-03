package com.hokyozu.kyofuse.chat.repository;

import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    // directUserOne/directUserTwo devem ser passados já ordenados pelo chamador.
    Optional<Conversation> findByDirectUserOneAndDirectUserTwo(User directUserOne, User directUserTwo);

    Optional<Conversation> findByCommunity(Community community);

    List<Conversation> findByCommunityIn(java.util.List<Community> communities);

    @Query(
        value = """
        SELECT c FROM Conversation c
        LEFT JOIN FETCH c.directUserOne u1
        LEFT JOIN FETCH c.directUserTwo u2
        WHERE c.type = :type AND (c.directUserOne = :user OR c.directUserTwo = :user)
        """,
        countQuery = """
            SELECT COUNT(c) FROM Conversation c
            WHERE c.type = :type AND (c.directUserOne = :user OR c.directUserTwo = :user)
        """
    )
    Page<Conversation> findAllByTypeAndDirectUser(@Param("type") ConversationType type, @Param("user") User user, Pageable pageable);
}
