package com.hokyozu.kyofuse.notifications.repository;

import com.hokyozu.kyofuse.notifications.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Mais recentes primeiro: é a ordem esperada de uma caixa de notificações, e sem
    // isso a ordem fica a cargo do banco.
    Page<Notification> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<Notification> findAllByUserId(UUID userId);
}
