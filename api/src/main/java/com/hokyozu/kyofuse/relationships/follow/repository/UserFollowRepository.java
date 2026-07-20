package com.hokyozu.kyofuse.relationships.follow.repository;

import com.hokyozu.kyofuse.relationships.follow.entity.UserFollow;
import com.hokyozu.kyofuse.relationships.follow.enums.FollowStatus;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {
    boolean existsByFollowerAndFollowed(User user, User followedUser);

    Page<UserFollow> findAllByFollower(User followedUser, Pageable pageable);

    Page<UserFollow> findAllByFollowed(User user, Pageable pageable);

    UserFollow findByFollowerAndFollowed(User user, User followedUser);

    boolean existsByFollowerAndFollowedAndStatus(User sender, User receiver, FollowStatus followStatus);
}
