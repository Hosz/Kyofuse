package com.hokyozu.kyofuse.chat.entity;

import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.chat.enums.DirectConversationStatus;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private ConversationType type;

    @Column(name = "name", length = 80)
    private String name;

    /** Só preenchido em conversas GROUP — ver V27 e doc.md 10.6. */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direct_user_one_id")
    private User directUserOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direct_user_two_id")
    private User directUserTwo;

    @Enumerated(EnumType.STRING)
    @Column(name = "direct_message_status", length = 20)
    private DirectConversationStatus directMessageStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
