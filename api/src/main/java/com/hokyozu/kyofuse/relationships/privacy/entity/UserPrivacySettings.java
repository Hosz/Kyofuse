package com.hokyozu.kyofuse.relationships.privacy.entity;

import com.hokyozu.kyofuse.relationships.privacy.enums.*;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_privacy_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrivacySettings {

    @Id
    @Column(name = "user_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_visibility", length = 40, nullable = false)
    private ProfileVisibility profileVisibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "posts_visibility", length = 40, nullable = false)
    private ProfileVisibility postsVisibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "followers_visibility", length = 40, nullable = false)
    private ProfileVisibility followersVisibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "following_visibility", length = 40, nullable = false)
    private ProfileVisibility followingVisibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_permission", length = 40, nullable = false)
    private MessagePermission messagePermission;

    @Enumerated(EnumType.STRING)
    @Column(name = "friend_request_permission", length = 40, nullable = false)
    private FriendRequestPermission friendRequestPermission;

    @Enumerated(EnumType.STRING)
    @Column(name = "follow_permission", length = 40, nullable = false)
    private FollowPermission followPermission;

    @Enumerated(EnumType.STRING)
    @Column(name = "team_invite_permission", length = 40, nullable = false)
    private TeamInvitePermission teamInvitePermission;

    @Enumerated(EnumType.STRING)
    @Column(name = "duo_invite_permission", length = 40, nullable = false)
    private DuoInvitePermission duoInvitePermission;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
