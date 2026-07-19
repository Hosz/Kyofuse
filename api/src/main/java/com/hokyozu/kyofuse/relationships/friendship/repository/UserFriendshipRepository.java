package com.hokyozu.kyofuse.relationships.friendship.repository;

import com.hokyozu.kyofuse.relationships.friendship.entity.UserFriendship;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserFriendshipRepository extends JpaRepository<UserFriendship, UUID> {
    Page<UserFriendship> findAllByUserOneOrUserTwo(User user, User user1, Pageable pageable);
}
