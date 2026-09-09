package com.hokyozu.kyofuse.notifications.repository;

import com.hokyozu.kyofuse.notifications.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Mais recentes primeiro: é a ordem esperada de uma caixa de notificações, e sem
    // isso a ordem fica a cargo do banco.
    @EntityGraph(attributePaths = {"user", "actor"})
    Page<Notification> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "actor"})
    List<Notification> findAllByUserId(UUID userId);

    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.status = com.hokyozu.kyofuse.notifications.enums.NotificationStatus.READ, n.readAt = :now
        WHERE n.user.id = :userId AND n.status = com.hokyozu.kyofuse.notifications.enums.NotificationStatus.UNREAD
    """)
    int markAllAsRead(@Param("userId") UUID userId, @Param("now") Instant now);

    long countByUserIdAndStatus(UUID userId, com.hokyozu.kyofuse.notifications.enums.NotificationStatus status);

    List<Notification> findAllByUserIdAndTargetTypeAndTargetId(
            UUID userId,
            com.hokyozu.kyofuse.notifications.enums.NotificationTargetType targetType,
            UUID targetId
    );
}
