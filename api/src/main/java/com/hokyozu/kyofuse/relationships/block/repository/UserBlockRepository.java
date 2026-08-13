package com.hokyozu.kyofuse.relationships.block.repository;

import com.hokyozu.kyofuse.relationships.block.entity.UserBlock;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserBlockRepository extends JpaRepository<UserBlock, UUID> {
    Page<UserBlock> findAllByBlocker(User user, Pageable pageable);

    UserBlock findByBlockerAndBlocked(User user, User blocked);

    boolean existsByBlockerAndBlocked(User user, User blocked);

    boolean existsByBlockedAndBlocker(User blocked, User blocker);
}
