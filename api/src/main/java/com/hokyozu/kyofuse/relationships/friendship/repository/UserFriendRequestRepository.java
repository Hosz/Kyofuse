package com.hokyozu.kyofuse.relationships.friendship.repository;

import com.hokyozu.kyofuse.relationships.friendship.entity.UserFriendRequest;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserFriendRequestRepository extends JpaRepository<UserFriendRequest, UUID> {
    boolean existsBySenderAndReceiver(User user, User userFriendRequest);

    Page<UserFriendRequest> findAllByReceiver(User user, Pageable pageable);

    Page<UserFriendRequest> findAllBySender(User user, Pageable pageable);
}
