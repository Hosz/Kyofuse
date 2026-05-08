package com.hokyozu.kyofuse.notifications.entity;

import com.hokyozu.kyofuse.notifications.enums.NotificationStatus;
import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY,  optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actorId;

    @Enumerated(EnumType.STRING)
    @Column(length = 60, nullable = false)
    private NotificationType type;

    @Column(length = 160, nullable = false)
    private String title;

    @Column(length = 500, nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 40, nullable = false)
    private NotificationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 60)
    private NotificationTargetType targetType;

    @Column(name = "target_id")
    private UUID targetId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json", columnDefinition = "jsonb")
    private Map<String, Object> metadataJson;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime readAt;
}
