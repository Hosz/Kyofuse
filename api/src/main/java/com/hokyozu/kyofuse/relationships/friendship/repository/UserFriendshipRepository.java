package com.hokyozu.kyofuse.relationships.friendship.repository;

import com.hokyozu.kyofuse.relationships.friendship.entity.UserFriendship;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserFriendshipRepository extends JpaRepository<UserFriendship, UUID> {
    Page<UserFriendship> findAllByUserOneOrUserTwo(User user, User user1, Pageable pageable);

    boolean existsByUserOneAndUserTwo(User owner, User view);

    @Query("""
        select (count(f) > 0) from UserFriendship f
        where (f.userOne = :first and f.userTwo = :second)
            or (f.userOne = :second and f.userTwo = :first)
    """)
    boolean existsMutualFriendship(User first, User second);

    /** Versão em lote de existsMutualFriendship: devolve, dentre :others, os que têm
     *  amizade mútua com :user — o lado oposto da amizade, seja qual for a coluna. */
    @Query("""
        select case when f.userOne = :user then f.userTwo.id else f.userOne.id end
        from UserFriendship f
        where (f.userOne = :user and f.userTwo in :others)
            or (f.userTwo = :user and f.userOne in :others)
    """)
    List<UUID> findMutualFriendIdsIn(User user, List<User> others);

    @Query("""
        select f from UserFriendship f
        where (f.userOne = :user and f.userTwo = :friend)
           or (f.userOne = :friend and f.userTwo = :user)
    """)
    java.util.Optional<UserFriendship> findFriendshipBetween(User user, User friend);

    UserFriendship findByUserOneAndUserTwo(User user, User friend);

    @Query("""
        select count(f) from UserFriendship f
        where f.userOne = :user or f.userTwo = :user
    """)
    long countTotalFriends(User user);

    long countUserFriendshipByUserOne(User userOne);
}
