package com.hokyozu.kyofuse.relationships.block.repository;

import com.hokyozu.kyofuse.relationships.block.entity.UserBlock;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserBlockRepository extends JpaRepository<UserBlock, UUID> {
    Page<UserBlock> findAllByBlocker(User user, Pageable pageable);

    UserBlock findByBlockerAndBlocked(User user, User blocked);

    boolean existsByBlockerAndBlocked(User user, User blocked);

    boolean existsByBlockedAndBlocker(User blocked, User blocker);

    @Query("select b.blocked.id from UserBlock b where b.blocker = :user")
    List<UUID> findBlockedIdsByBlocker(User user);

    @Query("select b.blocker.id from UserBlock b where b.blocked = :user")
    List<UUID> findBlockerIdsByBlocked(User user);
}
