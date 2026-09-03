package com.hokyozu.kyofuse.relationships.follow.repository;

import com.hokyozu.kyofuse.relationships.follow.entity.UserFollow;
import com.hokyozu.kyofuse.relationships.follow.enums.FollowStatus;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {
    boolean existsByFollowerAndFollowed(User user, User followedUser);

    @Query("select f.followed.id from UserFollow f where f.follower = :follower")
    List<UUID> findFollowedIdsByFollower(User follower);

    @Query("select f.followed.id from UserFollow f where f.follower = :follower and f.followed in :followed")
    List<UUID> findFollowedIdsByFollowerAndFollowedIn(User follower, List<User> followed);

    @EntityGraph(attributePaths = {"follower", "followed"})
    Page<UserFollow> findAllByFollower(User followedUser, Pageable pageable);

    @EntityGraph(attributePaths = {"follower", "followed"})
    Page<UserFollow> findAllByFollowed(User user, Pageable pageable);

    UserFollow findByFollowerAndFollowed(User user, User followedUser);

    boolean existsByFollowerAndFollowedAndStatus(User sender, User receiver, FollowStatus followStatus);

    UserFollow findByFollowerAndFollowedAndStatus(User sender, User user, FollowStatus followStatus);

    Long countByFollowed(User followed);

    Long countByFollower(User follower);
}
