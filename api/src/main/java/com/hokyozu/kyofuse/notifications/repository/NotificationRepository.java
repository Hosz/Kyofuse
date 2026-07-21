package com.hokyozu.kyofuse.notifications.repository;

import com.hokyozu.kyofuse.notifications.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

    List<Notification> findAllByUserId(UUID userId);
}
