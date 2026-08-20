package com.hokyozu.kyofuse.chat.entity;

import com.hokyozu.kyofuse.chat.enums.ConversationMemberRole;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberStatus;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation_members")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private ConversationMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ConversationMemberStatus status;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

    @Column(name = "last_read_at")
    private Instant lastReadAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
