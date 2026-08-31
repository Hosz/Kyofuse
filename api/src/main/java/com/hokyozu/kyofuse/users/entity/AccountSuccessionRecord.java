package com.hokyozu.kyofuse.users.entity;

import com.hokyozu.kyofuse.users.enums.SuccessionEntityType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_succession_records")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSuccessionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 40, nullable = false)
    private SuccessionEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "successor_id")
    private User successor;

    @Column(name = "previous_member_type", length = 40)
    private String previousMemberType;

    @Column(name = "previous_role_in_team", length = 80)
    private String previousRoleInTeam;

    @Column(name = "previous_role", length = 40)
    private String previousRole;

    @Column(name = "previous_successor_member_type", length = 40)
    private String previousSuccessorMemberType;

    @Column(name = "previous_successor_role", length = 40)
    private String previousSuccessorRole;

    @Column(name = "was_owner", nullable = false)
    private boolean wasOwner;

    @Column(name = "previous_status", length = 40)
    private String previousStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
